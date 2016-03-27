package utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import prefuse.activity.ActivityManager;

public class Utils {
	/**
	 * Rotates an image. Actually rotates a new copy of the image.
	 * 
	 * @param img The image to be rotated
	 * @param angle The angle in degrees
	 * @return The rotated image
	 */
	public static BufferedImage rotate(BufferedImage img, double angle)
	{
	    double sin = Math.abs(Math.sin(Math.toRadians(angle))),
	           cos = Math.abs(Math.cos(Math.toRadians(angle)));

	    int w = img.getWidth(null), h = img.getHeight(null);

	    int neww = (int) Math.floor(w*cos + h*sin),
	        newh = (int) Math.floor(h*cos + w*sin);

	    BufferedImage bimg = new BufferedImage(neww, newh, BufferedImage.TYPE_INT_ARGB);//toBufferedImage(getEmptyImage(neww, newh));
	    Graphics2D g = bimg.createGraphics();

	    g.translate((neww-w)/2, (newh-h)/2);
	    g.rotate(Math.toRadians(angle), w/2, h/2);
	    g.drawRenderedImage(img, null);
	    g.dispose();

	    return bimg;
	}
	
	public static void waitUntilActivitiesAreFinished() {
		do
		{
			try {
				Thread.sleep(Constants.SLEEP_TIME);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		while(ActivityManager.activityCount() != 0);
	}
}
