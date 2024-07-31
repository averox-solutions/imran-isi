import {
  useCallback,
  useEffect,
} from 'react';
import {
  useReactiveVar,
  useLazyQuery,
  useMutation,
  useSubscription,
} from '@apollo/client';
import Auth from '/imports/ui/services/auth';
import useCurrentUser from '/imports/ui/core/hooks/useCurrentUser';
import useMeeting from '/imports/ui/core/hooks/useMeeting';
import { partition } from '/imports/utils/array-utils';
import { USER_AGGREGATE_COUNT_SUBSCRIPTION } from '/imports/ui/core/graphql/queries/users';
import {
  getSortingMethod,
  sortVideoStreams,
} from '/imports/ui/components/video-provider/stream-sorting';
import {
  useVideoState,
  getConnectingStream,
  setVideoState,
  useConnectingStream,
  streams,
  getVideoState,
} from '../state';
import {
  OWN_VIDEO_STREAMS_QUERY,
  GRID_USERS_SUBSCRIPTION,
  VIEWERS_IN_WEBCAM_COUNT_SUBSCRIPTION,
} from '../queries';
import videoService from '../service';
import { CAMERA_BROADCAST_STOP } from '../mutations';
import {
  GridItem,
  StreamItem,
  GridUsersResponse,
  OwnVideoStreamsResponse,
} from '../types';
import { DesktopPageSizes, MobilePageSizes } from '/imports/ui/Types/meetingClientSettings';
import logger from '/imports/startup/client/logger';
import useDeduplicatedSubscription from '/imports/ui/core/hooks/useDeduplicatedSubscription';
import { useMeetingIsBreakout } from '/imports/ui/components/app/service';
import useSettings from '/imports/ui/services/settings/hooks/useSettings';
import { SETTINGS } from '/imports/ui/services/settings/enums';
import { useStorageKey } from '/imports/ui/services/storage/hooks';
import ConnectionStatus from '/imports/ui/core/graphql/singletons/connectionStatus';
import { VIDEO_TYPES } from '/imports/ui/components/video-provider/enums';

const FILTER_VIDEO_STATS = [
  'outbound-rtp',
  'inbound-rtp',
];

export const useStatus = () => {
  const { isConnected, isConnecting } = useVideoState();
  if (isConnecting) return 'videoConnecting';
  if (isConnected) return 'connected';
  return 'disconnected';
};

export const useDisableReason = () => {
  const videoLocked = useIsUserLocked();
  const hasCapReached = useHasCapReached();
  const hasVideoStream = useHasVideoStream();
  const connected = useReactiveVar(ConnectionStatus.getConnectedStatusVar());
  const locks = {
    videoLocked,
    camCapReached: hasCapReached && !hasVideoStream,
    disconnected: !connected,
  };
  const locksKeys = Object.keys(locks);
  const disableReason = locksKeys
    .filter((i) => locks[i as keyof typeof locks])
    .shift();
  return disableReason;
};

export const useIsUserLocked = () => {
  const disableCam = useDisableCam();
  const { data: currentUser } = useCurrentUser((u) => ({
    locked: u.locked,
    isModerator: u.isModerator,
  }));
  return !!currentUser?.locked && !currentUser.isModerator && disableCam;
};

export const useVideoStreamsCount = () => {
  const { streams } = useStreams();

  return streams.length;
};

export const useLocalVideoStreamsCount = () => {
  const { streams } = useStreams();
  const localStreams = streams.filter((vs) => videoService.isLocalStream(vs.stream));

  return localStreams.length;
};

export const useInfo = () => {
  const { data } = useMeeting((m) => ({
    voiceSettings: {
      // @ts-expect-error -> There seems to be a design issue on the projection portion.
      voiceConf: m.voiceSettings?.voiceConf,
    },
  }));
  const voiceBridge = data?.voiceSettings ? data.voiceSettings.voiceConf : null;
  return {
    userId: Auth.userID as string,
    userName: Auth.fullname as string,
    meetingId: Auth.meetingID as string,
    sessionToken: Auth.sessionToken as string,
    voiceBridge,
  };
};

export const useHasCapReached = () => {
  const { data: meeting } = useMeeting((m) => ({
    meetingCameraCap: m.meetingCameraCap,
    usersPolicies: {
      // @ts-expect-error -> There seems to be a design issue on the projection portion.
      userCameraCap: m.usersPolicies?.userCameraCap,
    },
  }));
  const videoStreamsCount = useVideoStreamsCount();
  const localVideoStreamsCount = useLocalVideoStreamsCount();

  // If the meeting prop data is unreachable, force a safe return
  if (
    meeting?.usersPolicies === undefined
    || !meeting?.meetingCameraCap === undefined
  ) return true;
  const { meetingCameraCap } = meeting;
  const { userCameraCap } = meeting.usersPolicies;

  // @ts-expect-error -> There seems to be a design issue on the projection portion.
  const meetingCap = meetingCameraCap !== 0 && videoStreamsCount >= meetingCameraCap;
  const userCap = userCameraCap !== 0 && localVideoStreamsCount >= userCameraCap;

  return meetingCap || userCap;
};

export const useDisableCam = () => {
  const { data: meeting } = useMeeting((m) => ({
    lockSettings: {
      // @ts-expect-error -> There seems to be a design issue on the projection portion.
      disableCam: m.lockSettings?.disableCam,
    },
  }));
  return meeting?.lockSettings ? meeting?.lockSettings.disableCam : false;
};

export const usePageSizeDictionary = () => {
  const {
    desktopPageSizes: DESKTOP_PAGE_SIZES,
    mobilePageSizes: MOBILE_PAGE_SIZES,
  } = window.meetingClientSettings.public.kurento.pagination;

  const PAGINATION_THRESHOLDS_CONF = window.meetingClientSettings.public.kurento.paginationThresholds;
  const PAGINATION_THRESHOLDS_ENABLED = PAGINATION_THRESHOLDS_CONF.enabled;
  const PAGINATION_THRESHOLDS = PAGINATION_THRESHOLDS_CONF.thresholds.sort(
    (t1, t2) => t1.users - t2.users,
  );

  const { data: countData } = useDeduplicatedSubscription(
    USER_AGGREGATE_COUNT_SUBSCRIPTION,
  );
  const userCount = countData?.user_aggregate?.aggregate?.count || 0;
  // Dynamic page sizes are disabled. Fetch the stock page sizes.
  if (!PAGINATION_THRESHOLDS_ENABLED || PAGINATION_THRESHOLDS.length <= 0) {
    return !videoService.isMobile ? DESKTOP_PAGE_SIZES : MOBILE_PAGE_SIZES;
  }

  // Dynamic page sizes are enabled. Get the user count, isolate the
  // matching threshold entry, return the val.
  let targetThreshold;
  const processThreshold = (
    threshold: {
      desktopPageSizes?: DesktopPageSizes,
      mobilePageSizes?: MobilePageSizes,
    } = {
      desktopPageSizes: DESKTOP_PAGE_SIZES,
      mobilePageSizes: MOBILE_PAGE_SIZES,
    },
  ) => {
    // We don't demand that all page sizes should be set in pagination profiles.
    // That saves us some space because don't necessarily need to scale mobile
    // endpoints.
    // If eg mobile isn't set, then return the default value.
    if (!videoService.isMobile) {
      return threshold.desktopPageSizes || DESKTOP_PAGE_SIZES;
    }
    return threshold.mobilePageSizes || MOBILE_PAGE_SIZES;
  };

  // Short-circuit: no threshold yet, return stock values (processThreshold has a default arg)
  if (userCount < PAGINATION_THRESHOLDS[0].users) return processThreshold();

  // Reverse search for the threshold where our participant count is directly equal or great
  // The PAGINATION_THRESHOLDS config is sorted when imported.
  for (
    let mapIndex = PAGINATION_THRESHOLDS.length - 1;
    mapIndex >= 0;
    mapIndex -= 1
  ) {
    targetThreshold = PAGINATION_THRESHOLDS[mapIndex];
    if (targetThreshold.users <= userCount) {
      return processThreshold(targetThreshold);
    }
  }
  return undefined;
};

export const useMyRole = () => {
  const { data } = useCurrentUser((u) => ({ role: u.role }));
  return data?.role;
};

export const useMyPageSize = () => {
  const myRole = useMyRole();
  const pageSizes = usePageSizeDictionary();
  const ROLE_MODERATOR = videoService.getRoleModerator();
  const ROLE_VIEWER = videoService.getRoleViewer();
  let size;
  switch (myRole) {
    case ROLE_MODERATOR:
      size = pageSizes?.moderator;
      break;
    case ROLE_VIEWER:
    default:
      size = pageSizes?.viewer;
  }

  const actualSize = size ?? 0;

  useEffect(() => {
    setVideoState({ pageSize: actualSize });
  }, [actualSize]);

  return actualSize;
};

export const useIsPaginationEnabled = () => {
  const myPageSize = useMyPageSize();
  const { paginationEnabled } = useSettings(SETTINGS.APPLICATION) as { paginationEnabled?: boolean };
  return myPageSize > 0 && paginationEnabled;
};

export const useStreams = () => {
  const videoStreams = useReactiveVar(streams);
  return { streams: videoStreams };
};

export const useGridUsers = (exceptUserIds: string[], visibleStreamCount: number) => {
  const gridSize = useGridSize();
  const isGridEnabled = useStorageKey('isGridEnabled');

  const {
    data: gridData,
    error: gridError,
  } = useSubscription<GridUsersResponse>(
    GRID_USERS_SUBSCRIPTION,
    {
      variables: { exceptUserIds, limit: Math.max(gridSize - visibleStreamCount, 0) },
      skip: !isGridEnabled,
    },
  );

  if (gridError) {
    logger.error({
      logCode: 'grid_users_sub_error',
      extraInfo: {
        errorName: gridError.name,
        errorMessage: gridError.message,
      },
    }, 'Grid users subscription failed.');
  }

  let gridUsers: GridItem[] = [];

  if (gridData) {
    const newGridUsers = gridData.user.map((user) => ({
      ...user,
      type: VIDEO_TYPES.GRID,
    }));
    gridUsers = newGridUsers;
  }

  return gridUsers;
};

export const useSharedDevices = () => {
  const { streams } = useStreams();
  const devices = streams
    .filter((s) => videoService.isLocalStream(s.stream))
    .map((vs) => vs.deviceId);

  return devices;
};

export const useNumberOfPages = () => {
  const { numberOfPages } = useVideoState();
  return numberOfPages;
};

export const useCurrentVideoPageIndex = () => {
  const { currentVideoPageIndex } = useVideoState();
  return currentVideoPageIndex;
};

export const useGridSize = () => {
  let size;
  const myRole = useMyRole();
  const ROLE_MODERATOR = videoService.getRoleModerator();
  const ROLE_VIEWER = videoService.getRoleViewer();
  const {
    desktopGridSizes: DESKTOP_GRID_SIZES,
    mobileGridSizes: MOBILE_GRID_SIZES,
  } = window.meetingClientSettings.public.kurento.pagination;

  const gridSizes = !videoService.isMobile
    ? DESKTOP_GRID_SIZES
    : MOBILE_GRID_SIZES;

  switch (myRole) {
    case ROLE_MODERATOR:
      size = gridSizes.moderator;
      break;
    case ROLE_VIEWER:
    default:
      size = gridSizes.viewer;
  }

  return size;
};

export const useVideoStreams = () => {
  const { viewParticipantsWebcams } = useSettings(SETTINGS.DATA_SAVING) as { viewParticipantsWebcams?: boolean };
  const { currentVideoPageIndex, numberOfPages } = useVideoState();
  const { streams: videoStreams } = useStreams();
  const connectingStream = useConnectingStream(videoStreams);
  const myPageSize = useMyPageSize();
  const isPaginationEnabled = useIsPaginationEnabled();
  let streams: StreamItem[] = [...videoStreams];
  let totalNumberOfOtherStreams: number | undefined;

  const {
    paginationSorting: PAGINATION_SORTING,
    defaultSorting: DEFAULT_SORTING,
  } = window.meetingClientSettings.public.kurento.cameraSortingModes;

  if (connectingStream) streams.push(connectingStream);

  if (!viewParticipantsWebcams) {
    streams = streams.filter((vs) => videoService.isLocalStream(vs.stream));
  }

  if (isPaginationEnabled) {
    const [filtered, others] = partition(
      streams,
      (vs: StreamItem) => videoService.isLocalStream(vs.stream) || (vs.type === VIDEO_TYPES.STREAM && vs.user.pinned),
    );
    const [pin, mine] = partition(
      filtered,
      (vs: StreamItem) => vs.type === VIDEO_TYPES.STREAM && vs.user.pinned,
    );

    totalNumberOfOtherStreams = others.length;
    const chunkIndex = currentVideoPageIndex * myPageSize;
    const sortingMethod = (numberOfPages > 1) ? PAGINATION_SORTING : DEFAULT_SORTING;
    const paginatedStreams = sortVideoStreams(others, sortingMethod)
      .slice(chunkIndex, (chunkIndex + myPageSize)) || [];

    if (getSortingMethod(sortingMethod).localFirst) {
      streams = [...pin, ...mine, ...paginatedStreams];
    } else {
      streams = [...pin, ...paginatedStreams, ...mine];
    }
  } else {
    streams = sortVideoStreams(streams, DEFAULT_SORTING);
  }

  const gridUsers = useGridUsers(
    videoStreams.map((s) => s.userId),
    streams.length,
  );

  return {
    streams,
    gridUsers,
    totalNumberOfStreams: streams.length,
    totalNumberOfOtherStreams,
  };
};

export const useHasVideoStream = () => {
  const { streams } = useStreams();
  const connectingStream = useConnectingStream();
  return !!connectingStream || streams.some((s) => videoService.isLocalStream(s.stream));
};

const useOwnVideoStreamsQuery = () => useLazyQuery<OwnVideoStreamsResponse>(
  OWN_VIDEO_STREAMS_QUERY,
  {
    variables: {
      userId: Auth.userID,
      streamIdPrefix: `${videoService.getPrefix()}%`,
    },
  },
);

export const useExitVideo = (forceExit = false) => {
  const [cameraBroadcastStop] = useMutation(CAMERA_BROADCAST_STOP);
  const [getOwnVideoStreams] = useOwnVideoStreamsQuery();

  const exitVideo = useCallback(async () => {
    const { isConnected } = getVideoState();

    if (isConnected || forceExit) {
      const sendUserUnshareWebcam = (cameraId: string) => {
        return cameraBroadcastStop({ variables: { cameraId } });
      };

      return getOwnVideoStreams().then(async ({ data }) => {
        if (data) {
          const streams = data.user_camera || [];
          const results = streams.map((s) => sendUserUnshareWebcam(s.streamId));
          return Promise.all(results).then(() => {
            videoService.exitedVideo();
            return true;
          }).catch((e) => {
            logger.warn({
              logCode: 'exit_audio',
              extraInfo: e,
            }, 'Exiting audio');
            return false;
          });
        }
        return true;
      });
    }
    return true;
  }, [cameraBroadcastStop]);

  return exitVideo;
};

export const useViewersInWebcamCount = (): number => {
  const { data } = useDeduplicatedSubscription(VIEWERS_IN_WEBCAM_COUNT_SUBSCRIPTION);
  return data?.user_camera_aggregate?.aggregate?.count || 0;
};

export const useLockUser = () => {
  const exitVideo = useExitVideo();
  return useCallback(() => {
    const { isConnected } = getVideoState();
    if (isConnected) {
      exitVideo();
    }
  }, [exitVideo]);
};

export const useStopVideo = () => {
  const [cameraBroadcastStop] = useMutation(CAMERA_BROADCAST_STOP);
  const [getOwnVideoStreams] = useOwnVideoStreamsQuery();

  return useCallback(async (cameraId?: string) => {
    const { data } = await getOwnVideoStreams();
    const streams = data?.user_camera ?? [];
    const connectingStream = getConnectingStream();
    const hasTargetStream = streams.some((s) => s.streamId === cameraId);
    const hasOtherStream = streams.some((s) => s.streamId !== cameraId);

    if (hasTargetStream) {
      cameraBroadcastStop({ variables: { cameraId } });
    }

    if (!hasOtherStream && !connectingStream) {
      videoService.exitedVideo();
    } else {
      videoService.stopConnectingStream();
    }
  }, [cameraBroadcastStop]);
};

export const useActivePeers = () => {
  const videoData = useVideoStreams();

  if (!videoData) return null;

  const { streams: activeVideoStreams } = videoData;

  if (!activeVideoStreams) return null;

  const activePeers: Record<string, RTCPeerConnection> = {};

  activeVideoStreams.forEach((stream) => {
    if (videoService.webRtcPeersRef()[stream.stream]) {
      activePeers[stream.stream] = videoService.webRtcPeersRef()[stream.stream].peerConnection;
    }
  });

  return activePeers;
};

export const useGetStats = () => {
  const peers = useActivePeers();

  return useCallback(async () => {
    if (!peers) return null;

    const stats: Record<string, unknown> = {};

    await Promise.all(
      Object.keys(peers).map(async (peerId) => {
        const peerStats = await peers[peerId].getStats();

        const videoStats: Record<string, unknown> = {};

        peerStats.forEach((stat) => {
          if (FILTER_VIDEO_STATS.includes(stat.type)) {
            videoStats[stat.type] = stat;
          }
        });
        stats[peerId] = videoStats;
      }),
    );

    return stats;
  }, [peers]);
};

export const useShouldRenderPaginationToggle = () => {
  const myPageSize = useMyPageSize();
  const {
    paginationToggleEnabled: PAGINATION_TOGGLE_ENABLED,
  } = window.meetingClientSettings.public.kurento.pagination;

  return PAGINATION_TOGGLE_ENABLED && myPageSize > 0;
};

export const useIsVideoPinEnabledForCurrentUser = (isModerator: boolean) => {
  const isBreakout = useMeetingIsBreakout();
  const isPinEnabled = videoService.isPinEnabled();

  return !!(isModerator && isPinEnabled && !isBreakout);
};
