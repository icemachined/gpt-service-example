package com.example.gptserviceexample;

import jep.MainInterpreter;
import jep.SharedInterpreter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

@RestController
public class GptController {
    SharedInterpreter interp;
    GptController(){
        MainInterpreter.setSharedModulesArgv("",
                "--dataset=shakespeare",
                "--n_layer=4",
                "--n_head=4",
                "--n_embd=64",
                "--device=cpu",
                "--compile=False",
                "--eval_iters=1",
                "--block_size=64",
                "--batch_size=8");
        String relativeGPTmasterPath = "target/python/nanoGPT-master";
        String gptDir = null;
        try {
            gptDir = new File(relativeGPTmasterPath).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JepInitializer.INSTANCE.getConfig().addIncludePaths(gptDir);
        JepInitializer.INSTANCE.getConfig().addIncludePaths("$gptDir/data/shakespeare");
        SharedInterpreter.setConfig(JepInitializer.INSTANCE.getConfig());
        interp = new SharedInterpreter();
        interp.eval("import os");
        interp.eval("os.chdir('$relativeGPTmasterPath')");
    }
    @PostMapping
    String generatePoem(@RequestParam String start) {

    }
    @PostMapping
    String trainGPT() {
        interp.set("__file__", "prepare.py");
        interp.runScript("data/shakespeare_char/prepare.py");
        interp.eval("os.chdir('../..')");
        interp.runScript("train.py");
    }
}
