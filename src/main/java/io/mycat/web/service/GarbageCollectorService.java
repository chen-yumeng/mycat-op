package io.mycat.web.service;

import io.mycat.web.entity.GarbageCollectorBean;
import io.mycat.web.entity.MemoryPoolBean;

import java.util.List;

/**
 * @program: mycat->GarbageCollectorService
 * @description:
 * @author: cg
 * @create: 2020-06-12 14:14
 **/
public interface GarbageCollectorService {

    GarbageCollectorBean get();

    List<MemoryPoolBean> getPools();
}
