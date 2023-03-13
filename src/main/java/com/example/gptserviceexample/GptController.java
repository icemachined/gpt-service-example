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
    private final String relativeGPTmasterPath;
    GptController(){
        MainInterpreter.setSharedModulesArgv("",
                "config/train_shakespeare_char.py",
                "--log_interval=1",
                "--n_layer=4",
                "--n_head=4",
                "--n_embd=128",
                "--device=cpu",
                "--compile=False",
                "--eval_iters=20",
                "--block_size=64",
                "--batch_size=12",
                "--max_iters=2000",
                "--lr_decay_iters=2000",
                "--dropout=0.0");
        relativeGPTmasterPath = "build/python/nanoGPT-master";
        String gptDir;
        try {
            gptDir = new File(relativeGPTmasterPath).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JepInitializer.getConfig().addIncludePaths(gptDir);
        JepInitializer.getConfig().addIncludePaths("$gptDir/data/shakespeare_char");
        SharedInterpreter.setConfig(JepInitializer.getConfig());
    }

    @PostMapping(path="/gpt/poem")
    String generatePoem(@RequestParam String start) {
        SharedInterpreter interp = new SharedInterpreter();
        interp.set("__file__", "initgpt.py");
        interp.runScript("initgpt.py");
        return interp.invoke("generate_text", start).toString();
    }

    @PostMapping(path="/gpt/train")
    void trainGPT() {
        SharedInterpreter interp = new SharedInterpreter();
        interp.eval("import os");
        interp.eval("os.chdir('" + relativeGPTmasterPath + "')");
        interp.set("__file__", "prepare.py");
        interp.runScript("data/shakespeare_char/prepare.py");

        //interp.eval("os.chdir('../..')");
        interp.runScript("train.py");
        interp.close();
    }
}
