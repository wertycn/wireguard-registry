package icu.debug.net.wg.service.controller;

import icu.debug.net.wg.service.entity.GenerateRequest;
import icu.debug.net.wg.service.entity.GenerateResult;
import icu.debug.net.wg.service.entity.HttpResult;
import icu.debug.net.wg.service.module.WebConfigGenerateModule;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author hanjinxiang@debug.icu
 * @date 2024-02-07 3:04
 */
@RequestMapping("/v1/generate")
@RestController
public class GenerateController {

    private final WebConfigGenerateModule generateModule;

    public GenerateController(WebConfigGenerateModule generateModule) {
        this.generateModule = generateModule;
    }

    @PostMapping("/submit")
    public HttpResult<List<GenerateResult>> submit(@RequestBody GenerateRequest request) {
        List<GenerateResult> generate = this.generateModule.generate(request);
        return HttpResult.success(generate);
    }


}
