# Hospital Simulator

Using JavaFX and eduni distributions https://www.icsa.inf.ed.ac.uk/research/groups/hase/simjava/distributions/doc/eduni/distributions/Distributions.html

Screenshot:
![simu](https://user-images.githubusercontent.com/75932758/162035267-74cc185a-60e0-4e28-8514-43db285194a8.png)

If you don't understand Finnish, you can use google translate on phone and take a picture :)

Features:

- Simulate patients
- Simulate sick patients with various severities 
- Through sicknesses, simulate where the patient should go to in the hospital
- Simulate how long the patient survives without treatment
- Simulate how long the treatment lasts
- User can change how many sections the hospital has and how many patients come in
- User can change how many sections of the hospital are closed or opened at the start of the simulation
- The simulator opens closed sections after a while if there is demand
- User can change the order in which patients are handled. There is a priority list (very sick patients get priority, "ambulance queue") and a "dumb" list.
- User can change what is acceptable queue length for different sections of the hospital.
- User can follow live statistics (deaths, usage levels etc.) and also see information that is carried through multiple runs of the simulator.

If you want to test the simulator, there are JARs included. JDK required. Run hospitalSimulator.jar from hospitalSimulator_jar folder to test the simulation.

