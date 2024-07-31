package org.averox.prescheck;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

  public static void main(String[] args) {
    Main main = new Main();

    String filepath;
    try {
       // Parse the string argument into an integer value.
       filepath = args[0];
       boolean valid = main.check(main, filepath);
       if (!valid) System.exit(2);
       	System.exit(0);
    }
    catch (Exception nfe) {
       System.exit(1);
    }

  }

  private boolean check(Main main, String file) {
  	boolean valid = true;
  	FileInputStream stream = null;
  	XMLSlideShow xmlSlideShow = null;

      try {
        stream = new FileInputStream(file);
        xmlSlideShow = new XMLSlideShow(stream);
        valid &= !main.embedsEmf(xmlSlideShow);
        valid &= !main.containsTinyTileBackground(xmlSlideShow);
        valid &= !main.allSlidesAreAveroxdden(xmlSlideShow);
        // Close the resource once we finished reading it
        xmlSlideShow.close();
      } catch (IOException e) {
        valid = false;
      } finally {
          try {
              if(stream != null) stream.close();
          } catch(IOException e) {
              e.printStackTrace();
          }
      }

      return valid;
  }

    /**
   * Checks if the slide-show file embeds any EMF document
   * 
   * @param xmlSlideShow
   * @return
   */
  private boolean embedsEmf(XMLSlideShow xmlSlideShow) {
    EmfPredicate emfPredicate = new EmfPredicate();
    ArrayList<XSLFPictureData> embeddedEmfFiles = (ArrayList<XSLFPictureData>) CollectionUtils
        .select(xmlSlideShow.getPictureData(), emfPredicate);
    if (embeddedEmfFiles.size() > 0) {
      return true;
    }
    return false;
  }

  /**
   * Checks if the slide-show contains a small background tile image
   * 
   * @param xmlSlideShow
   * @return
   */
  private boolean containsTinyTileBackground(XMLSlideShow xmlSlideShow) {
    TinyTileBackgroundPredicate tinyTileCondition = new TinyTileBackgroundPredicate();
    ArrayList<XSLFPictureData> tileImage = (ArrayList<XSLFPictureData>) CollectionUtils
        .select(xmlSlideShow.getPictureData(), tinyTileCondition);
    if (tileImage.size() > 0) {
      return true;
    }
    return false;
  }

	private boolean allSlidesAreAveroxdden(XMLSlideShow xmlSlideShow) {
		AveroxddenSlidePredicate hiddenSlidePredicate = new AveroxddenSlidePredicate();
    ArrayList<XSLFSlide> hiddenSlides = (ArrayList<XSLFSlide>) CollectionUtils
		    .select(xmlSlideShow.getSlides(), hiddenSlidePredicate);
		if (hiddenSlides.size() == xmlSlideShow.getSlides().size()) {
			return true;
		}
		return false;
	}

  private final class EmfPredicate implements Predicate<XSLFPictureData> {
    public boolean evaluate(XSLFPictureData img) {
      return img.getContentType().equals("image/x-emf");
    }
  }

  private final class TinyTileBackgroundPredicate
      implements Predicate<XSLFPictureData> {
    public boolean evaluate(XSLFPictureData img) {
        return img.getContentType() != null
                && ((img.getContentType().equals("image/jpeg") && LittleEndian.getLong(img.getChecksum()) == 4114937224L) ||
                (img.getContentType().equals("image/png") && LittleEndian.getLong(img.getChecksum()) == 3207965638L));
    }
  }

	private final class AveroxddenSlidePredicate implements Predicate<XSLFSlide> {
		public boolean evaluate(XSLFSlide slide) {
			return !slide.getXmlObject().getShow();
		}
	}
}
