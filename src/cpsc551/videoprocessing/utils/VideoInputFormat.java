package cpsc551.videoprocessing.utils;
// cc VideoInputFormat An InputFormat for reading a whole file as a record

import java.io.IOException;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.*;


//vv VideoInputFormat
public class VideoInputFormat
    extends FileInputFormat<Text, Image> {
  
  //prevent splitting the file
  @Override
  protected boolean isSplitable(JobContext context, Path file) {
    return false;
  }

  //
  @Override
  public RecordReader<Text, Image> createRecordReader(
      InputSplit split, TaskAttemptContext context) throws IOException,
      InterruptedException {
    VideoRecordReader reader = new VideoRecordReader();
    reader.initialize(split, context);
    return reader;
  }
}
//^^ VideoInputFormat
