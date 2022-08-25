package com.code.util;

import com.code.config.streamconfig.ConfigConstant;
import com.code.filemanageweb.upload.domain.Range;
import com.code.filemanageweb.upload.service.StreamServiceImpl;
import com.ruoyi.common.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IO--closing, getting file name ... main function method
 */
public class IoUtil {
    static final Pattern RANGE_PATTERN = Pattern.compile("bytes \\d+-\\d+/\\d+");
    public static final int BUFFER_LENGTH = 1024 * 1024 * 10;


    /**
     * According the key, generate a file (if not exist, then create
     * a new file).
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public static File getFile(String filename,String path) throws IOException {
        if (filename == null || filename.isEmpty()){
            return null;
        }

        if(StringUtils.isEmpty(path)){
            path = ConfigConstant.fileRepository;
        }

        String name = filename.replaceAll("/", Matcher.quoteReplacement(File.separator));
        File f = new File( path + File.separator + name);
        if (!f.getParentFile().exists()){
            f.getParentFile().mkdirs();
        }
        if (!f.exists()){
            f.createNewFile();
        }

        return f;
    }

    /**
     * 20220322增加path传递
     * Acquired the file.
     *
     * @param key
     * @return
     * @throws IOException
     */
    public static File getTokenedFile(String key,String path) throws IOException {
        if (key == null || key.isEmpty()){
            return null;
        }

        if(StringUtils.isEmpty(path)){
            path = ConfigConstant.fileRepository;
        }
        File f = new File( path+ File.separator + key);
        if (!f.getParentFile().exists()){
            f.getParentFile().mkdirs();
        }
        if (!f.exists()){
            f.createNewFile();
        }

        return f;
    }

    public static void storeToken(String key,String path) throws IOException {
        if (key == null || key.isEmpty()){
            return;
        }
        if(StringUtils.isEmpty(path)){
            path = ConfigConstant.fileRepository;
        }
        File f = new File(path + File.separator + key);
        if (!f.getParentFile().exists()){
            f.getParentFile().mkdirs();
        }
        if (!f.exists()){
            f.createNewFile();
        }
    }

    /**
     * close the IO stream.
     *
     * @param stream
     */
    public static void close(Closeable stream) {
        try {
            if (stream != null){
                stream.close();
            }
        } catch (IOException e) {
        }
    }

    /**
     * 获取Range参数
     *
     * @param req
     * @return
     * @throws IOException
     */
    public static Range parseRange(HttpServletRequest req) throws IOException {
        String range = req.getHeader(StreamServiceImpl.CONTENT_RANGE_HEADER);
        Matcher m = RANGE_PATTERN.matcher(range);
        if (m.find()) {
            range = m.group().replace("bytes ", "");
            String[] rangeSize = range.split("/");
            String[] fromTo = rangeSize[0].split("-");

            long from = Long.parseLong(fromTo[0]);
            long to = Long.parseLong(fromTo[1]);
            long size = Long.parseLong(rangeSize[1]);

            return new Range(from, to, size);
        }
        throw new IOException("Illegal Access!");
    }

    /**
     * From the InputStream, write its data to the given file.
     */
//    public static long streaming(InputStream in, String key, String fileName) throws IOException {
//        OutputStream out = null;
//        File f = getTokenedFile(key);
//        try {
//            out = new FileOutputStream(f);
//
//            int read = 0;
//            final byte[] bytes = new byte[BUFFER_LENGTH];
//            while ((read = in.read(bytes)) != -1) {
//                out.write(bytes, 0, read);
//            }
//            out.flush();
//        } finally {
//            close(out);
//        }
//        /** rename the file * fix the `renameTo` bug */
//        File dst = IoUtil.getFile(fileName);
//        dst.delete();
//        f.renameTo(dst);
//
//        long length = getFile(fileName).length();
//        /** if `STREAM_DELETE_FINISH`, then delete it. */
//        if (Configurations.isDeleteFinished()) {
//            dst.delete();
//        }
//
//        return length;
//    }
}
