package io.mycat.web.controller;

import io.mycat.web.service.MycatSchemaServer;
import io.mycat.web.utils.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: Mycat->MycatScheamController
 * @description:
 * @author: cg
 * @create: 2020-06-15 18:01
 **/
@RestController
public class MycatScheamController {

    @Autowired
    private MycatSchemaServer schemaServer;

    @GetMapping("/mycat/properties/schema/getMycatSchemaConfig")
    public ResultCode getMycatSchemaConfig() {
        try {
            return new ResultCode(200, schemaServer.getSchemaConfig());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }
}
