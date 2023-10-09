# Example of running complex neural network from Spring Boot service

This example shows how to run [Nano GPT](https://github.com/karpathy/nanoGPT) from Spring Boot microservice using [JEP](https://github.com/ninia/jep) technology.
To run this application you can use bootRun task, or start manually GptServiceExampleApplication from IDE.
Before run you need to perform build task to install [jep-distro](https://github.com/icemachined/jep-distro) module from Maven Central and [Nano GPT](https://github.com/karpathy/nanoGPT) sources from github master branch  
Then open local Swagger frontend http://localhost:8080/swagger-ui/index.html
You can use 2 endpoints: <br>
1. /gpt/train
2. /gpt/poem

Here you need train nural network first, and then generate a poem based on your first phrase.


## Debugging Python Code
You can debug it using python remote debugger.

1. Install python plugin
2. To enable debugging you need to setup DEBUG_PYTHON_EGG environment variable with path to pydevd-pycharm.egg.
This is pydevd-pycharm code, so you don't need to install it via pip. It is always matched with python plugin version. 
Usually it is located in IntelliJ IDEA python plugin directory, 
for example on Windows: <br>`DEBUG_PYTHON_EGG=%APPDATA%\JetBrains\IntelliJIdea2022.3\plugins\python\debugger-eggs-output\pydevd-pycharm.egg` <br>
You can also set the host and port via `DEBUG_PYTHON_HOST` and `DEBUG_PYTHON_PORT`, respectively. Otherwise, those will default to `localhost` and `52225`.
3. Now, add a "Run/Debug Configuration" in Intellij and choose the "Python Debug Server". Configure the host name and port according to the values set above.
You should also set a proper "Path mapping" according to your local configuration. For example:<br>
Local path : `D:/Workspace/joker/gpt-service-example/build/python/nanoGPT-master` <br>
Remote path: `D:/Workspace/joker/gpt-service-example/build/python/nanoGPT-master` <br>
Local path : `D:/Workspace/joker/gpt-service-example/src/main/resources` <br>
Remote path: `D:/Workspace/joker/gpt-service-example/build/resources/main` <br>
4. Start the Python Debug Server (run configuration prepared above).
5. Set breakpoint in some python code you want to inspect
6. Run your code `GptServiceExampleApplication`. 
7. Start training or generate poem through swagger. It will connect to remote debugger and stop on break point where you can inspect the state.

## Profiling Python Code
You can switch on python profiling by setting environment variable: <br>
PROFILER_STAT_FILE - file name of debug output for example `profiler_stats.gprof` <br>
and set the following system property:<br>
`-Dpython.interpreters.number=1` - to make processing in one thread for debug output dump to be happened. <br>
This initializes *profile* decorator which uses embedded Python *cProfiler*
You can use `@profile` annotation to place on any function you need to profile.
After finishing, profiling statistics will be dumped into PROFILER_STAT_FILE.
You can use https://sourceforge.net/projects/qcachegrindwin/ to analyse this output.
To open it in qcachegrind/kcachegrind you need to convert it to callgrind format
<br> `pip install pyprof2calltree` <br>
`pyprof2calltree -i profiler_stats.gprof -o profiler_stats.kgrind`

also you can convert from gprof to dot<br> 
`pip install gprof2dot` <br>
`gprof2dot -f pstats profile_results.prof | dot -Tpng -o profile_results.png`