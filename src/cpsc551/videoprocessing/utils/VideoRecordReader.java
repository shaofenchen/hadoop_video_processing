// cc VideoRecordReader RecordReader for decomposite the video to frames
package cpsc551.videoprocessing.utils;
import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import cpsc551.videoprocessing.utils.*;

import com.googlecode.javacv.*;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;

//vv VideoRecordReader
class VideoRecordReader extends RecordReader<Text, Image> {
	
	public static final String LocalTempPath = "/home/hduser/InputVideos/";
	public static final String LocalOutputMainPath = "/home/hduser/OutputFrames/";
	public static final String AbstractorPath = "/home/codeforces/main";
	
	private FileSplit fileSplit;
	private String tempFilepath;
	private String filenameWithoutExt;
	private OpenCVFrameGrabber grabber;
	private Configuration conf;
	private Image value = new Image();
	private Text key;
	private Integer frameCounter = 0;
	private boolean processed = false;

	@Override
	public void initialize(InputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
		this.fileSplit = (FileSplit) split;
		this.conf = context.getConfiguration();
		Path file = fileSplit.getPath();
		String filename = file.getName();
		this.filenameWithoutExt = filename.substring(0, filename.length() - 4);
		//String filepath_str = file.toString();
		FileSystem fs = file.getFileSystem(conf);
		this.tempFilepath = LocalTempPath + filename;
		fs.copyToLocalFile(file, new Path(tempFilepath));
        this.grabber = new OpenCVFrameGrabber(tempFilepath);
        System.out.println(tempFilepath + filename);
        try {
			grabber.start();
			System.out.println("start of video:" + filename);
			//IplImage grabbedImage = grabber.grab();
			//cvSaveImage("/home/hduser/OutputFrames/image01.jpg", grabbedImage);
		} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		if (!processed) {
			try {
				IplImage grabbedImage;
				if((grabbedImage = grabber.grab()) != null)
				{
					this.key = new Text(LocalOutputMainPath + filenameWithoutExt + frameCounter.toString() + ".jpg");
					this.value = new Image(grabbedImage);
					
					this.frameCounter++;
					
					System.out.println(key.toString());
					//cvSaveImage(key.toString(), grabbedImage);
				}
				else{
					this.processed = true;
					System.out.println("end of video:" + filenameWithoutExt);
					grabber.stop();
				}
			} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
				//e.printStackTrace();
				this.processed = true;
				try {
					grabber.stop();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("end of video:" + filenameWithoutExt);
			}
			
			return true;
		}
		return false;
	}

	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
		return key;
	}

	@Override
	public Image getCurrentValue() throws IOException, InterruptedException {
		return value;
	}

	@Override
	public float getProgress() throws IOException {
		return processed ? 1.0f : 0.0f;
	}

	@Override
	public void close() throws IOException {
		// do nothing
	}
}
// ^^ VideoRecordReader
