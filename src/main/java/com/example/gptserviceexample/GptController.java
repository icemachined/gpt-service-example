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
        try {
            relativeGPTmasterPath = new File("build/python/nanoGPT-master").getCanonicalPath().replace('\\', '/');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JepInitializer.getConfig().addIncludePaths(relativeGPTmasterPath);
        JepInitializer.getConfig().addIncludePaths(relativeGPTmasterPath + "/data/shakespeare_char");
        SharedInterpreter.setConfig(JepInitializer.getConfig());
    }

    @PostMapping(path="/gpt/poem")
    String generatePoem(@RequestParam String start) {
        SharedInterpreter interp = new SharedInterpreter();
        interp.eval("import os");
        interp.eval("os.chdir('" + relativeGPTmasterPath + "')");
        String initGptFile = JepInitializer.gptInitFile.toString();
        interp.set("__file__", initGptFile);
        interp.runScript(initGptFile);
        return interp.invoke("generate_text", start).toString();
    }

    @PostMapping(path="/gpt/train")
    void trainGPT() {
        SharedInterpreter interp = new SharedInterpreter();
        interp.eval("import os");
        interp.eval("os.chdir('" + relativeGPTmasterPath + "')");
        System.out.println(interp.getValue("os.getcwd()"));
        String prepFile = "data/shakespeare_char/prepare.py";
        interp.set("__file__", prepFile);
        interp.runScript(prepFile);

        //interp.eval("os.chdir('../..')");
        interp.set("__file__", "train.py");
        interp.runScript("train.py");
        interp.close();
    }
}
