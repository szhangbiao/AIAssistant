package cn.booslink.llm.common.loader;

import org.libpag.PAGFile;

public interface IPAGLoader {

    PAGFile getPagFile(String name);

    void putPagFile(String name, PAGFile pagFile);

    void release();
}
