Yanfeng Jin (Tony) & Uriel Ulloa - HW2
All group members were present and contributing to during all work on this project.
We have neither given nor received any unauthorized aid in this project. 

SudokuPlayer.java is needed to run the program. 
No known bugs. :)

Costumized solver:
We modified the backtracking search with AC3. Instead of always running backtrack on the next cell, we run the backtracking search on the cell with the most constraints (that has the smallest domain). It performed exceptionally better on the medium and hard case than the original algorithm, since it took less than a second to solve the medium and hard cases, whereas the original algorithm took seconds or minutes. We could further improve it with forward checking if we had the time. 
