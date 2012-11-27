// cc VideoProc A MapReduce program for face detection from videos
import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
//import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
//import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;

import cpsc551.videoprocessing.utils.*;

//vv VideoProc
public class VideoProc {
	
	static class VideoProcessMapper extends
			Mapper<Text, Image, Text, Image> {
		//haar classifier to be used
		static String classifierName = "resource/haarcascade_frontalface_alt.xml";
		
		//private Text filenameKey;

		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			//InputSplit split = context.getInputSplit();
			//Path path = ((FileSplit) split).getPath();
			//filenameKey = new Text(path.toString());
		}

		@Override
		protected void map(Text key, Image value, Context context)
				throws IOException, InterruptedException {
			IplImage grabbedImage = value.getImage();
	        // Preload the opencv_objdetect module to work around a known bug.
	        Loader.load(opencv_objdetect.class);

	        CvHaarClassifierCascade classifier = new CvHaarClassifierCascade(cvLoad(classifierName));
	        if (classifier.isNull()) {
	            System.err.println("Error loading classifier file \"" + classifierName + "\".");
	            System.exit(1);
	        }
	        CvMemStorage storage = CvMemStorage.create();
	        int width  = grabbedImage.width();
	        int height = grabbedImage.height();
	        IplImage grayImage    = IplImage.create(width, height, IPL_DEPTH_8U, 1);
            cvCvtColor(grabbedImage, grayImage, CV_BGR2GRAY);
            
            //face detection
            CvSeq faces = cvHaarDetectObjects(grayImage, classifier, storage,
                    1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
            int total = faces.total();
            for (int i = 0; i < total; i++) {
    			CvRect r = new CvRect(cvGetSeqElem(faces, i));
    			cvRectangle(grabbedImage, cvPoint(r.x(), r.y()),
    					cvPoint(r.x() + r.width(), r.y() + r.height()),
    					CvScalar.YELLOW, 1, CV_AA, 0);
            }
            //if face detected, save the frame
            if(faces.total() > 0){
            	context.write(key, value);
            	cvSaveImage(key.toString(), grabbedImage);
            }
			// context.write(filenameKey, value);
		}

	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: VideoProc <input path> <output path>");
			System.exit(-1);
		}

		Job job = new Job();
		job.setJarByClass(VideoProc.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.setMapperClass(VideoProcessMapper.class);
		// job.setReducerClass(IdentityReducer.class);
		job.setInputFormatClass(VideoInputFormat.class);
		// job.setOutputFormatClass(SequenceFileOutputFormat.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Image.class);

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

}
// ^^ VideoProc
