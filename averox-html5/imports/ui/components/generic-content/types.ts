import { GenericContentMainArea } from 'averox-html-plugin-sdk';
import { GenericContentMainArea as GenericContentMainAreaLayout } from '../layout/layoutTypes';

export interface GenericContentMainAreaContainerProps {
    genericMainContentId: string;
}

export interface GenericContentMainAreaProps {
    isResizing: boolean;
    genericContentId: string;
    genericContentLayoutInformation: GenericContentMainAreaLayout;
    renderFunctionComponents: GenericContentMainArea[];
}

export interface GenericContentSidekickContainerProps {
    genericSidekickContentId: string;
}

export interface GenericSidekickContentProps {
    layoutContextDispatch: (...args: unknown[]) => void;
    genericContentId: string;
    renderFunction: (element: HTMLElement) => void;
    genericContentLabel: string;
}
