# CS725: ASSIGNMENT 2 RL + POMDP README

[TOC]

Refer to the report file `cs725_report.pdf` for documentation. The sections of the assignment is separated into two parts with their own source files. The first part is a java program found in the directory named `rlResults/source` and the second part is model files for an existing POMDP solver found in the directory named `pomdpResults/*`

## PT 1 RL Java program

### Compile & Run

To compile and run the java program follow the steps as follows:

1. Open up a terminal instance
2. Change directory to: `../rlResults/source`
3. Use the Java compiler command: `javac *.java`
4. Run the program with the command: `java -cp *.class`
5. If this does not work follow plan B

#### Plan B: Compile and Run

If command line does not work then using IDE to run program will work just as fine

1. Open up Eclipse Java IDE
2. Create a new Java project under: `File > New > Project...`
3. Import the source files from `../rlResults/source` into the project
4. Run `RLearning.java` as a Java Application

### Operating the program

The program will open up a GUI in a Java JFrame window. Prompts in the terminal will ask for user entry to start the RL process.

`Press enter to start learning process...` 

*Note: If user wishes to watch the learning process, open up the `RLearning.java` source file and change the `boolean watchLearning` variable at the top of the file from true. While doing this process, preamble variables such as alpha and gamma values can be changed here.*

Once the learning process is done. The yellow circle representing the robot will be continuously following the learnt action policy.  (Spawn in random location and navigate towards the goal and re-spawn in another random location repeatedly) This demonstrates the action policy generated from the RL process.

`Episodes over. Showing policies.`



## PT 2 POMDP Planning

Model and solved policy are available in the `pomdpResults` directory. The graphical representation is shown in  file `robotSearchGraph.pdf`

### Solve and simulation

In order to solve the model the pre-existing solver is required first and can be obtained here:

`http://bigbird.comp.nus.edu.sg/pmwiki/farm/appl/`

Download and install the "offline" version of the Approximate POMDP Planning Software (APPL). Follow the instructions to make the executables used for this model application. These executables to be found in the `../src` file are as follows:

* `pomdpconvert`
* `pomdpsol`
* `pomdpsim`
* `pomdpeval`
* `polgraph`

The top file can be held in directory `pomdpResults` as an example set of commands.

Open up a terminal instance:

1. Convert the `.pomdp ` model file into the `.pomdpx` format as follows:

```bash
$ ./pomdpconvert ../source/robotSearch.pomdp
```

2. Solve the problem with:

```bash
$ ./pomdpsol ../source/robotSearch.pomdpx
```

3. Simulate the policy with :

```bash
$ ./pomdpsim --simLen 100 --simNum 1000 --policy-file ../source/out.policy ../source/robotSearch.pomdpx
```

4. Generate graphical representation of the policy with:

```bash
$ ./polgraph --policy-file ../source/out.policy --policy-graph ../source/robotSearch.dot ../source/robotSearch.pomdpx
```

5. Using Graphviz (http://www.graphviz.org/) the `.dot` can be converted into a more common read format such as `.pdf`:

```bash
$ graphviz -Tpdf ../source/robotSearch.dot -o ../source/robotSearchGraph.pdf
```
All the generated files should be located in the `../source` directory.