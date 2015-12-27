package main

import (
	"fmt"
	"github.com/codegangsta/cli"
	"io/ioutil"
	"os"
	"os/exec"
	"path"
	"strings"
	"sync"
)

const (
	Version        = "2.2.1"
	KickAssJarPath = "/Applications/KickAssembler/KickAss.jar"
	ViceX64Path    = "/Applications/VICE/X64.app/Contents/MacOS/x64"
	OutputDir      = "bin"
	X              = "java -jar '/Applications/KickAssembler/KickAss.jar' $1'.asm' -log 'bin/'$1'_BuildLog.txt' -o 'bin/'$1'.prg' -vicesymbols -showmem -symbolfiledir bin && /Applications/VICE/X64.app/Contents/MacOS/x64 -autostart 'bin/'$1'.prg' +confirmexit -moncommands 'bin/'$1'.vs'"
)

func main() {
	app := cli.NewApp()
	app.Version = Version
	app.Name = "c64spec"
	app.Usage = "Command line runner of c64spec test files."
	app.HideVersion = true
	app.Flags = []cli.Flag{
		cli.StringFlag{
			Name:   "config, c",
			Usage:  "path to config file",
			EnvVar: "C64SPEC_CONFIG_FILE",
		},
		cli.BoolFlag{
			Name:  "version, v",
			Usage: "print the version",
		},
	}
	app.Action = func(ctx *cli.Context) {
		var ch chan string = make(chan string)
		wg := &sync.WaitGroup{}

		wg.Add(len(ctx.Args()))

		if len(ctx.Args()) > 0 {
			for i := 0; i < len(ctx.Args()); i++ {
				go testFile(ctx.Args()[i], ch, wg)
			}
		}

		go func() {
			for i := range ch {
				fmt.Println(i)
			}
		}()

		wg.Wait()
	}

	app.Run(os.Args)
}

func testFile(filePath string, c chan string, wg *sync.WaitGroup) {
	defer wg.Done()
	var result = ""
	result += "Testing: " + filePath + "\n"
	result += "  compilation:"
	output, success := compileSingleFile(filePath)
	if !success {
		c <- result + output
		return
	}
	result += " ok\n"
	result += "  running tests:"
	runTest(filePath)
	result += readResultFile(filePath) + "\n"
	c <- result
}
func compileSingleFile(filePath string) (string, bool) {
	cmd := exec.Command("java", "-jar", KickAssJarPath, filePath, "-o", outputFilePath(filePath), "-symbolfiledir", OutputDir, "-vicesymbols", "-libdir", "/Users/mehowte/code/private/c64/64spec/lib", "-libdir", "/Users/mehowte/code/private/c64/std64/lib", ":on_exit=jam", ":write_final_results_to_file=true", ":result_file_name="+resultFileName(filePath))
	//cmd.CombinedOutput()
	output, err := cmd.CombinedOutput()
	//printError(err)
	//printOutput(output)
	if err == nil && !strings.Contains(strings.ToLower(string(output)), "error") {
		return string(output), true
	} else {
		return string(output), false
	}
}

func runTest(filePath string) {
	//"/Applications/VICE/X64.app/Contents/MacOS/x64 -autostart 'bin/'$1'.prg' +confirmexit -moncommands 'bin/'$1'.vs'"
	//cmd := exec.Command(ViceX64Path, "-autostart", outputFilePath(filePath), "+confirmexit", "-moncommands", viceSymbolsFilePath(filePath))
	//cmd := exec.Command("/Applications/Wine.app/Contents/MacOS/Wine", "-autostart", outputFilePath(filePath), "+confirmexit", "-moncommands", viceSymbolsFilePath(filePath))
	cmd := exec.Command("/Applications/Wine.app/Contents/Resources/bin/wine", "/Applications/WinVICE-2.4.20-x86-r29904/WinVICE-2.4.20-x86-r29904/x64.exe", "-warp", "-autostart", outputFileName(filePath), "+confirmexit", "-moncommands", viceSymbolsFileName(filePath), "-autostartprgmode", "0", "-jamaction", "5", "-chdir", "./"+OutputDir)
	cmd.CombinedOutput()
	//printError(err)
	//printOutput(output)
}

func viceSymbolsFileName(filePath string) string {
	return fileNameOnly(filePath) + ".vs"
}

func viceSymbolsFilePath(filePath string) string {
	return path.Join(OutputDir, viceSymbolsFileName(filePath))
}

func outputFileName(filePath string) string {
	return fileNameOnly(filePath) + ".prg"
}
func outputFilePath(filePath string) string {
	return path.Join(OutputDir, outputFileName(filePath))
}

func resultFileName(filePath string) string {
	return strings.Replace(fileNameOnly(filePath), "_", "-", -1) + ".txt"
}
func resultFilePath(filePath string) string {
	return path.Join(OutputDir, resultFileName(filePath))
}

func readResultFile(filePath string) string {
	dat, err := ioutil.ReadFile(resultFilePath(filePath))
	check(err)
	return string(dat)
}

func check(e error) {
	if e != nil {
		panic(e)
	}
}

func fileNameOnly(filePath string) string {
	var fileNameWithExt = path.Base(filePath)
	var extension = path.Ext(fileNameWithExt)
	var fileName = fileNameWithExt[0 : len(fileNameWithExt)-len(extension)]
	return fileName
}

func printError(err error) {
	if err != nil {
		os.Stderr.WriteString(fmt.Sprintf("==> Error: %s\n", err.Error()))
	}
}

func printOutput(outs []byte) {
	if len(outs) > 0 {
		fmt.Printf("==> Output: %s\n", string(outs))
	}
}
