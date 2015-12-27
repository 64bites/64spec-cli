# 64spec-cli

A working prototype of a command line runner for 64spec

Disclaimer. This is my first ever go program, made in a few hours. So I don't even remember at the moment how to setup the environment to make it compile ;)

## Requirements
VICE-2.4.20-x86-r29904 or newer

On mac you can use WinVICE and Wine.

## Installation
Pull requests are welcome :D

## How it works

It takes one or more 64spec test file names as an input.

Then in parallel:
  
  1. each file is compiled with kick assembler and following commands are passed to 64spec 
    * :on_exit=jam // This one jams VICE after tests finish
    * :write_final_results_to_file=true // This one tells 64spec to write results to a file on a disk in drive 9
    * :result_file_name=<output filename> // This one sets the name of that file
  2. After successful compilation prg file is passed to VICE with following commands
    * -jamaction 5 // This ensures that VICE closes after JAM
    * -chdir ./<output_dir> // This one set's the current dir.

VICE mounts the output_dir as a drive 9. So 64spec can actually write a file outside of the emulator:)
Once each file is processed. The runner gathers results and prints them on the screen.

Earlier versions of VICE do not support exiting on JAM so there is no way to close the emulator after tests finish. 

If you know of any better way to do that let me know. Or even better make a pull request.

## Why Go?

It seems to be an easiest way to write multiplatform command line applications. I made it in just few hours without any prior knowledge of go and made the tests run in parallel in next 20 minutes:)


