package cpsc551.videoprocessing.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;

import com.googlecode.javacpp.BytePointer;
import com.googlecode.javacv.cpp.opencv_core.IplImage;


import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

public class Image implements Writable {

	private static final Log LOG = LogFactory.getLog(Image.class);

	// IPL image
	private IplImage image = null;

	public Image() {
	}

	// Create Image from IplImage
	public Image(IplImage image) {
		this.image = image;
	}
	
	// Create empty Image
	public Image(int height, int width, int depth, int nChannels){
		this.image = cvCreateImage(cvSize(width, height), depth, nChannels);
	}


	public IplImage getImage() {
		return image;
	}


	// Pixel depth in bits
	// PL_DEPTH_8U - Unsigned 8-bit integer
	// IPL_DEPTH_8S - Signed 8-bit integer
	// IPL_DEPTH_16U - Unsigned 16-bit integer
	// IPL_DEPTH_16S - Signed 16-bit integer
	// IPL_DEPTH_32S - Signed 32-bit integer
	// IPL_DEPTH_32F - Single-precision floating point
	// IPL_DEPTH_64F - Double-precision floating point
	public int getDepth() {
		return image.depth();
	}

	// Number of channels.
	public int getNumChannel() {
		return image.nChannels();
	}

	// Image height in pixels
	public int getHeight() {
		return image.height();
	}

	// Image width in pixels
	public int getWidth() {
		return image.width();
	}

	// The size of an aligned image row, in bytes
	public int getWidthStep() {
		return image.widthStep();
	}

	// Image data size in bytes.
	public int getImageSize() {
		return image.imageSize();
	}
	
	//@Override
	public void readFields(DataInput in) throws IOException {
		// Read image information
		int height = WritableUtils.readVInt(in);
		int width = WritableUtils.readVInt(in);
		int depth = WritableUtils.readVInt(in);
		int nChannels = WritableUtils.readVInt(in);
		int imageSize = WritableUtils.readVInt(in);

		// Read image bytes
		byte[] bytes = new byte[imageSize];
		in.readFully(bytes, 0, imageSize);

		image = cvCreateImage(cvSize(width, height), depth, nChannels);
		image.imageData(new BytePointer(bytes));
	}

	//@Override
	public void write(DataOutput out) throws IOException {
		// Write image information
		WritableUtils.writeVInt(out, image.height());
		WritableUtils.writeVInt(out, image.width());
		WritableUtils.writeVInt(out, image.depth());
		WritableUtils.writeVInt(out, image.nChannels());
		WritableUtils.writeVInt(out, image.imageSize());
		
		// Write image bytes
		ByteBuffer buffer = image.getByteBuffer();
		while (buffer.hasRemaining()) {
			out.writeByte(buffer.get());
		}
	}

}
