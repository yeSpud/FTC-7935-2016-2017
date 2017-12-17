/*
Copyright (c) 2016 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESSFOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.teamcode;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;

import com.qualcomm.ftcrobotcontroller.R;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.vuforia.Image;
import com.vuforia.Matrix34F;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Tool;
import com.vuforia.Vec3F;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CloseableFrame;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static android.R.attr.path;


/**
 * This OpMode is an example which should display the X Y and Z offsets from the target
 */

@Autonomous(name="Vuforia Draw/Save Frame Test", group ="Test")
@Disabled
public class VuforiaFrameDrawTest extends LinearOpMode {
	@Override
	public void runOpMode() throws InterruptedException {

		VuforiaLocalizer.Parameters params = new VuforiaLocalizer.Parameters(R.id.cameraMonitorViewId);
		params.vuforiaLicenseKey = "AWbzKHD/////AAAAGbGEq1ADgkVssDlzbJne3JxpbC88knTtSCe2QqAfzYWx8tyZlkO1jwQT7T2HMKxvxFXuNVlr4lgmOlx8xOFE8+4tMHH+4fHj/r4vtqwel1noNNRWvMR4H0SIJfhpiyAVrweptR90dULCME5CZB2kOWmnl/cnT+9vf0rw21wF9ydAnP4UDxp1PFkzIz/6C3FG+xVTtD3RfsM1NcRnu/qgKDSXqtOv+Dy5MIAKLV2nramvFI/PiL1O3yWNaVYpSQiMmZcE7wn8GNWmJA5MDf1QZItNcIfz0MqX/8X8mqqTiSYdQ34O4Cwj1OjYDeOyqOlVwD9nfvzX1uTMk7w8Hzh270GphNhEULGF+ilLzXNwLSkV";
		params.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
		VuforiaLocalizer vuforia = ClassFactory.createVuforiaLocalizer(params);
		Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true);
		vuforia.setFrameQueueCapacity(1); //tells VuforiaLocalizer to only store one frame at a time
		VuforiaTrackables beacons = vuforia.loadTrackablesFromAsset("FTC_2016-17");
		//makes object VuforiaTrackables called beacons but it comes from the asset FTC_2016-17.xml
		VuforiaTrackableDefaultListener wheels = ((VuforiaTrackableDefaultListener) beacons.get(0).getListener());
		Image rgb = null;
		long numImages;
		final String TAG = "ROBOT";


		/** Wait for the game to begin */
		telemetry.addData(">", "Press Play to start tracking");
		telemetry.update();
		waitForStart();
		VectorF translation = null;
		int count = 0;
		/** Start tracking the data sets we care about. */
		beacons.activate();
		while (opModeIsActive()) {
			CloseableFrame frame = vuforia.getFrameQueue().take(); //takes the frame at the head of the queue
			numImages = frame.getNumImages();
			for (int i = 0; i < numImages; i++) { //finds a frame that is in color, not grayscale
				if (frame.getImage(i).getFormat() == PIXEL_FORMAT.RGB565) {
					rgb = frame.getImage(i);
					break;
				}
			}
			OpenGLMatrix pose = wheels.getRawPose();
			if (rgb != null && rgb.getPixels() != null) {
				Bitmap bm = Bitmap.createBitmap(rgb.getWidth(), rgb.getHeight(), Bitmap.Config.RGB_565);
				bm = bm.copy(Bitmap.Config.RGB_565, true);
				bm.copyPixelsFromBuffer(rgb.getPixels());
				if (count % 100 == 0) { //draw on and save every 100 images
					Canvas canvas = new Canvas(bm);
					Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
					paint.setColor(Color.rgb(255, 0, 0));
					paint.setTextSize(14);
					canvas.drawText("TEST", bm.getWidth()/2, bm.getHeight()/2, paint);
					if (pose != null) { // if vuforia sees its target
						Matrix34F rawPose = new Matrix34F();
						rawPose.setData(Arrays.copyOfRange(pose.transposed().getData(), 0, 12));
						float corners[][] = new float[4][2];
						corners[0] = Tool.projectPoint(vuforia.getCameraCalibration(), rawPose, new Vec3F(0, 0, 0)).getData(); //projects the center point of the vuforia target onto the 2d array of pixels, returning an array of size 2
						canvas.drawCircle(corners[0][0], corners[0][1], 20, paint); //draws a red circle at the vuforia image center
						Log.i(TAG, "Drew Red Circle Successfully");
					}
					String path = Environment.getExternalStorageDirectory().toString(); //sets
					//telemetry.addData("Path", path);
					Log.i("Path", path);
					File file = new File(path, "VuforiaFrame"+count/100+".png"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
					FileOutputStream out = null;
					try {
						out = new FileOutputStream(file);
						bm.compress(CompressFormat.JPEG, 70, out); // bmp is your Bitmap instance
						// PNG is a lossless format, the compression factor (100) is ignored
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							if (out != null) {
								out.close();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				count++;
				telemetry.addData("Loops", count);
				frame.close();
			}
			else if (rgb == null && rgb.getPixels() == null) {
				telemetry.addData("Error", "rgb = null and rgb.getPixels() = null");
			}
			else if (rgb == null) {
				telemetry.addData("Error", "rgb = null");
			}
			else
			{
				telemetry.addData("Error", "rgb.getPixels() = null");
			}
			telemetry.update();
		}
		telemetry.addData("OpMode No Longer Active", " ");
		telemetry.update();
	}

	public static int red(int color) {
		return (color >> 16) & 0xFF;
	}

	public static int green(int color) {
		return (color >> 8) & 0xFF;
	}

	public static int blue(int color) {
		return color & 0xFF;
	}
}
