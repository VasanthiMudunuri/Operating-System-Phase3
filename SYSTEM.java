/*
SYSTEM class :
It is the main class that has the control of execution of entire project.
It reads the input job line by line, executes all the instructions and 
returns the output of the job.
It has been implemented according to the specifications given.
*/
/*
GLOBAL VARIABLES :
'inputJob' contains path of the input file passed as an argument 
which contains instructions in HEX.
'inputDataSegmentSize' to hold the number of inputs in the given job.
'outputDataSegmentSize' to hold the number of outputs expected in the 
given job.
'startingAddress' is the starting address indicating where the current 
job should be loaded.
'traceSwitch' is the trace flag.
'jodId' is the current job ID.
'initialProgramCounter' is the initial instruction of the current job
to be executed.
'jobSize' is the size of the current job.
'clock' is the System clock.
'iotime' is the input/output time.
'segmentFaultTime' to calculate segment fault time.
'pageFualtTime' to calculate page fault time.
'errorMessage' is the message to be displayed in case of an error.
'terminatingMessage' is the message indicating type of job termination 
whether abnormal or normal.
'availablePageFrames' holds the indexes of the available frames in Disk.
'readyQueue' to hold jobs that are ready to execute.
*/
package Vasanthi_Mudunuri_OS_Phase2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SYSTEM 
{
public static String inputJob = "";
public static int jobId=0;
public static String errorMessage="";
public static String terminatingMessage="Normal Termination";
public static List<Integer> availablePageFramesInMemory;
public static List<Integer> availablePageFramesInDisk;
public static List<Integer> availablePageFrames;
public static int lineNumber=0;
public static FileWriter outputFileWriter;
public static BufferedWriter outputBufferedWriter;
public static int meanTurnAroundTime=0;
public static int meanWaitingTime=0;
public static int meanPageFaults=0;
public static int meanDiskUtilization=0;
public static int meanMemoryUtilization=0;
public static int cpuTime[];
public static int jobTurnAroundTime[];
public static int codeSegment[];
public static int inputSegment[];
public static int outputSegment[];
public static int cpuShots[];
public static int IO[];

public static void main(String[] args) throws Exception
{
DISK.initialDiskFrameStatus();
MEMORY.initialMemoryPageFrameStatus();
SYSTEM.inputJob = args[0];
try
{
File output=new File("C:\\Users\\welcome\\Desktop\\Execution_Profile.txt");
if(output.exists())
{
output.delete();
}
output.createNewFile();
outputFileWriter=new FileWriter(output);
outputBufferedWriter=new BufferedWriter(outputFileWriter);
}
catch(Exception e)
{
if(e instanceof FileNotFoundException)
{
/*check if the output path exists - error*/
System.out.println("output path does not exist");
System.exit(0);
}
}
loadJobInInputFile();
SCHEDULER.scheduler();
}
public static void loadJobInInputFile()
{
String readLine=null;
String jobContent="";
try
{
BufferedReader bufferedReader = new BufferedReader(new FileReader(SYSTEM.inputJob));
for(int i=0;i<lineNumber;i++)
{
bufferedReader.readLine();
}
while((readLine=bufferedReader.readLine())!=null)
{
if(readLine.contains("**JOB"))
{
jobId+=1;
lineNumber++;
PCB pcb=new PCB();
pcb.arrivalTime+=CPU.clock;
String[] Job = readLine.split(" ");
pcb.inputDataSegmentSize=Integer.parseInt(Job[1],16);
pcb.outputDataSegmentSize=Integer.parseInt(Job[2],16);
readLine=bufferedReader.readLine();
lineNumber++;
String[] jobDetails=readLine.split(" ");
pcb.jobId=jobId;
if(jobDetails.length<=2)
{
ERROR_HANDLER.Error_Handler(1);
skipErrorJob(pcb);
}
pcb.jobIdInPacket=jobDetails[0];
DISK.createDiskTable(pcb.jobId+"0");
if(pcb.inputDataSegmentSize>0)
{
DISK.createDiskTable(pcb.jobId+"1");
}
DISK.createDiskTable(pcb.jobId+"2");
pcb.startingAddress=Integer.parseInt(jobDetails[1],16);
pcb.initialProgramCounter=jobDetails[2];
pcb.jobSize=Integer.parseInt(jobDetails[3],16);
pcb.traceSwitch=jobDetails[4];
if(pcb.traceSwitch.equals("1"))
{
pcb.writeTrace(pcb);
}
pcb.jobLengthInPages=(pcb.jobSize+pcb.inputDataSegmentSize+pcb.outputDataSegmentSize)/8;
pcb.numberOfJobPageFrames=Math.min(6,pcb.jobLengthInPages+2);
if(getIndexOfAvailablePageFramesInMemory().size()>=pcb.numberOfJobPageFrames)
{
pcb.jobPageFrames=allocateMemoryPageFramesToJob(pcb);
while((readLine=bufferedReader.readLine())!=null)
{
jobContent+=readLine;
lineNumber++;
if(jobContent.contains("**FIN") && pcb.inputDataSegmentSize>0 && !(jobContent.contains("**INPUT")))
{
ERROR_HANDLER.Error_Handler(24);
skipErrorJob(pcb);	
}
if(jobContent.contains("**FIN"))
{
jobContent=jobContent.substring(0,jobContent.indexOf("**FIN"));
loadJobToDisk(pcb,jobContent);
jobContent="";
break;
}
else if(jobContent.contains("**JOB") && !(jobContent.contains("**FIN")))
{
ERROR_HANDLER.Error_Handler(25);
skipErrorJob(pcb);
lineNumber--;
}
}
}
else
{
lineNumber-=2;
jobId-=1;
break;
}
}
else if(readLine.contains("**INPUT") || readLine.contains("**FIN"))
{
jobId+=1;
PCB pcb=new PCB();
pcb.jobId=jobId;
ERROR_HANDLER.Error_Handler(23);
skipErrorJob(pcb);
}
}
bufferedReader.close();
}
catch (Exception e) 
{
if (e instanceof FileNotFoundException) 
{
/*file not found - error*/
System.out.println("Input file not found");
System.exit(0);
}
}
}
public static void loadJobToDisk(PCB pcb,String content)
{
availablePageFrames=DISK.getIndexOfAvailablePageFramesInDisk();
int availablePageFrameIndex=0;
int pageNumber=0;
int programIndex=0;
int programPages=0;
int inputPages=0;
int outputPages=0;
String programContent="";
String inputContent="";
if(pcb.jobSize%8>0)
{
programPages=(pcb.jobSize/8)+1;	
}
else
{
programPages=pcb.jobSize/8;	
}
if(pcb.inputDataSegmentSize>0 && pcb.inputDataSegmentSize/8>=0 && pcb.inputDataSegmentSize%8==0)
{
inputPages=pcb.inputDataSegmentSize/8;	
}
else if(pcb.inputDataSegmentSize>0 && pcb.inputDataSegmentSize/8>=0 && pcb.inputDataSegmentSize%8>0)
{
inputPages=(pcb.inputDataSegmentSize/8)+1;	
}
if(pcb.outputDataSegmentSize>0 && pcb.outputDataSegmentSize/8>=0 && pcb.outputDataSegmentSize%8==0)
{
outputPages=pcb.outputDataSegmentSize/8;
}
else if(pcb.outputDataSegmentSize>0 && pcb.outputDataSegmentSize/8>=0 && pcb.outputDataSegmentSize%8>0)
{
outputPages=(pcb.outputDataSegmentSize/8)+1;	
}
pcb.diskPages=programPages+inputPages+outputPages;
if(availablePageFrames.size()>=pcb.diskPages)
{
if(pcb.inputDataSegmentSize>0 && content.contains("**INPUT"))
{
programContent=content.substring(0,content.indexOf("**INPUT"));
inputContent=content.substring(content.indexOf("**INPUT")+7,content.length());
if(inputContent.length()/4!=pcb.inputDataSegmentSize)
{
/*wrong number of input items - error*/
ERROR_HANDLER.Error_Handler(26);
skipErrorJob(pcb);
}
while(programIndex<programContent.length())
{
if(pageNumber==programPages-1)
{
DISK.savePageToDisk(pcb,pcb.jobId+"0",pageNumber,availablePageFrames.get(availablePageFrameIndex),programContent.substring(programIndex,programContent.length()));
programIndex+=32;
pageNumber+=1;
availablePageFrameIndex+=1;
}
else
{
DISK.savePageToDisk(pcb,pcb.jobId+"0",pageNumber,availablePageFrames.get(availablePageFrameIndex),programContent.substring(programIndex,programIndex+32));
programIndex+=32;
pageNumber+=1;
availablePageFrameIndex+=1;
}
}
if(inputPages==1)
{
DISK.savePageToDisk(pcb,pcb.jobId+"1", pageNumber,availablePageFrames.get(availablePageFrameIndex),inputContent);
pageNumber+=1;
availablePageFrameIndex+=1;
}
else
{
int inputIndex=0;
while(inputIndex<inputContent.length())
{
if(pageNumber==(programPages+inputPages)-1)
{
DISK.savePageToDisk(pcb,pcb.jobId+"1", pageNumber,availablePageFrames.get(availablePageFrameIndex),inputContent.substring(inputIndex,inputContent.length()));
inputIndex+=32;
pageNumber+=1;
availablePageFrameIndex+=1;	
}
else
{
DISK.savePageToDisk(pcb,pcb.jobId+"1", pageNumber,availablePageFrames.get(availablePageFrameIndex),inputContent.substring(inputIndex,inputIndex+32));
inputIndex+=32;
pageNumber+=1;
availablePageFrameIndex+=1;
}
}
}
while(outputPages>=1)
{
DISK.savePageToDisk(pcb,pcb.jobId+"2", pageNumber,availablePageFrames.get(availablePageFrameIndex),"");
outputPages-=1;
pageNumber+=1;
availablePageFrameIndex+=1;	
}
SCHEDULER.readyQueue.add(pcb);
}
else if(pcb.inputDataSegmentSize==0 && !(content.contains("**INPUT")))
{
programContent=content.substring(0,content.length());
while(programIndex<programContent.length())
{
if(pageNumber==programPages-1)
{
DISK.savePageToDisk(pcb,pcb.jobId+"0",pageNumber,availablePageFrames.get(availablePageFrameIndex),programContent.substring(programIndex,programContent.length()));
programIndex+=32;
pageNumber+=1;
availablePageFrameIndex+=1;
}
else
{
DISK.savePageToDisk(pcb,pcb.jobId+"0",pageNumber,availablePageFrames.get(availablePageFrameIndex),programContent.substring(programIndex,programIndex+32));
programIndex+=32;
pageNumber+=1;
availablePageFrameIndex+=1;
}
}
while(outputPages>=1)
{
DISK.savePageToDisk(pcb,pcb.jobId+"2", pageNumber,availablePageFrames.get(availablePageFrameIndex),"");
outputPages-=1;
pageNumber+=1;
availablePageFrameIndex+=1;	
}
SCHEDULER.readyQueue.add(pcb);
}
}
}
public static int[] allocateMemoryPageFramesToJob(PCB pcb)
{
availablePageFramesInMemory=getIndexOfAvailablePageFramesInMemory();
int[] allocatedPageFrames=new int[pcb.numberOfJobPageFrames];
for(int i=0;i<pcb.numberOfJobPageFrames;i++)
{
allocatedPageFrames[i]=availablePageFramesInMemory.get(i);
MEMORY.freeMemoryBitVector[allocatedPageFrames[i]]=1;
}
return allocatedPageFrames;
}
public static int[] allocateDiskPageFramesToJob(PCB pcb)
{
availablePageFramesInDisk=DISK.getIndexOfAvailablePageFramesInDisk();
int[] allocatedPageFrames=new int[pcb.diskPages];
for(int i=0;i<pcb.diskPages;i++)
{
allocatedPageFrames[i]=availablePageFramesInDisk.get(i);
MEMORY.freeMemoryBitVector[allocatedPageFrames[i]]=1;
}
return allocatedPageFrames;
}
public static List<Integer> getIndexOfAvailablePageFramesInMemory()
{
List<Integer> availablePageFrames=new ArrayList<Integer>();
for(int i=0;i<MEMORY.freeMemoryBitVector.length;i++)
{
if(MEMORY.freeMemoryBitVector[i]==0)
{
availablePageFrames.add(i);
}
}
return availablePageFrames;
}
public static void saveFile()
{
try 
{
outputBufferedWriter.close();
} 
catch (IOException e) 
{
e.printStackTrace();
}
} 
public static void skipErrorJob(PCB pcb)
{
pcb.errorMessage=SYSTEM.errorMessage;
pcb.terminatingMessage=SYSTEM.terminatingMessage;
pcb.generateJobDetails(pcb);
pcb.generateJobOutputFile(pcb);
pcb.clearMemoryAndDiskJobFrames(pcb);
try
{
BufferedReader bufferedReader = new BufferedReader(new FileReader(SYSTEM.inputJob));
String readLine=null;
for(int i=0;i<lineNumber;i++)
{
bufferedReader.readLine();
}
while((readLine=bufferedReader.readLine())!=null)
{
if(readLine.contains("**JOB"))
{
break;
}
lineNumber++;
}
bufferedReader.close();
}
catch(Exception e)
{
/*check if the output path exists - error*/
System.out.println("output path does not exist");
System.exit(0);
}
loadJobInInputFile();
}
public static void meteringAndReportingFacility()
{
try
{
outputBufferedWriter.write("Current value of the clock                          :"+Integer.toHexString(CPU.clock).toUpperCase()+"(HEX)");
outputBufferedWriter.newLine();
outputBufferedWriter.write("The number of jobs processed during a simulation run:"+jobId);
outputBufferedWriter.newLine();











outputBufferedWriter.write("The number of jobs that terminated normally         :"+CPU.terminatedNormalJobCount);
outputBufferedWriter.newLine();
outputBufferedWriter.write("The number of jobs that terminated abnormally       :"+(jobId-CPU.terminatedNormalJobCount));
outputBufferedWriter.newLine();
outputBufferedWriter.write("Mean turn around time                               :"+meanTurnAroundTime/CPU.terminatedNormalJobCount);
outputBufferedWriter.newLine();
outputBufferedWriter.write("Mean number of page faults                          :"+meanPageFaults/CPU.terminatedNormalJobCount);
outputBufferedWriter.newLine();
outputBufferedWriter.write("Mean waiting time of jobs terminated normally       :"+meanWaitingTime/CPU.terminatedNormalJobCount);
outputBufferedWriter.newLine();
outputBufferedWriter.write("Mean waiting time of jobs terminated normally       :"+meanWaitingTime/CPU.terminatedNormalJobCount);
outputBufferedWriter.newLine();
outputBufferedWriter.write("Mean memory utilization over all sampling inetrvals :"+meanMemoryUtilization/CPU.samplingInterval+" %");
outputBufferedWriter.newLine();
outputBufferedWriter.write("Mean disk utilization over all sampling inetrvals   :"+meanDiskUtilization/CPU.samplingInterval+" %");
outputBufferedWriter.newLine();
}
catch(Exception e)
{
/*check if output path exists - error*/
System.out.println("output path does not exist");
System.exit(0);	
}
}
}