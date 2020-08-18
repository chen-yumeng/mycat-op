package io.mycat.web.utils;

import io.mycat.web.service.impl.MycatServerConfigServiceImpl;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.core.io.ClassPathResource;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @program: Mycat->XMLServer
 * @description:
 * @author: cg
 * @create: 2020-08-16 16:10
 **/
public class XMLLoaderUtils {

    private static final SAXReader reader = new SAXReader();

    public static Document getInstance() {
        try {
            return reader.read(MycatServerConfigServiceImpl.class.getClassLoader().getResource("server.xml").getFile());
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void save(Document doc) {
        //指定文件输出的位置
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new ClassPathResource("server.xml").getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //指定写出的格式
        //格式好的格式.有空格和换行.
        OutputFormat format = OutputFormat.createPrettyPrint();
        //2.指定生成的xml文档的编码
        format.setEncoding("utf-8");
        //创建写出对象
        XMLWriter writer = null;
        try {
            writer = new XMLWriter(out, format);
            //写出对象
            writer.write(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //关闭流
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
