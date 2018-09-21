/*
PCB class :
Every job in the system will be represented by a process control block.
generatePMT() is used to output PMT table for every 15vtu.
generateOutputFile() is used to send information into the output file.
clearMemoryAndDiskJobFrames() is used to clear the occupied page frames 
in memory and disk by the job.
*/
/*
GLOBAL VARIABLES :
'jobId' holds the value of the job id.
'programSegmentInformation' is the pointer to the PMT table of the program 
segment.
'inputDataSegmentInformation' is the pointer to the PMT table of the input 
data segment.
'outputDataSegmentInformation' is the pointer to the PMT table of the output 
data segment.
'jobPageFrames' contains the index of the allocated page frames in memory 
to job.
*/
package Vasanthi_Mudunuri_OS_Phase2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class PCB 
{
public int jobId;
public int clock=0;
public int iotime = 0;
public int segmentFaultTime=0;
public int pageFaultTime=0;
public String programSegmentInformation;
public String inputDataSegmentInformation;
public String outputDataSegmentInformation;
public String jobIdInPacket = "";
public int inputDataSegmentSize=0;
public int outputDataSegmentSize=0;
public int startingAddress;
public String initialProgramCounter = "";
public int jobSize;
public String traceSwitch = "";
public int numberOfJobPageFrames;
public int[] jobPageFrames=new int[numberOfJobPageFrames];
public int diskPages;
public int[] diskPageFrames=new int[diskPages];
public int allocatedPageFrameIndex=1;
public int memoryPageFrameIndex=0;
public char[][] pcbStack=new char[7][16];
public int topOfStack=-1;
public BufferedWriter outputBufferedWriter;
public String errorMessage="";
public String terminatingMessage="Normal Termination";
public int readMemoryAddress;
public int writeMemoryAddress;
public int writeOutputLength=0;
public int outputPageNumber;
public int inputPageNumber;
public int readInputLength=0;
public int inputCount=0;
public int outputCount=0;
public int jobLengthInPages;
public int clockToWriteOutput=0;
public File traceFile;
public FileWriter fileWriter;
public BufferedWriter traceWriter;
public int clockForCpu=0;
public HashMap<Integer,Integer> memoryPageFrameStatus=new HashMap<Integer,Integer>();
public boolean loadFirstPageFlag=false;
public int getPage;
public int arrivalTime=0;
public int departureTime=0;
public int numberOfPageFaults;
public int burstTime;

public void generateJobDetails(PCB pcb)
{
try
{
outputBufferedWriter=SYSTEM.outputBufferedWriter;
outputBufferedWriter.write("JOB ID                  :\t"+pcb.jobId+"(DECIMAL)");
outputBufferedWriter.newLine();
} 
catch (Exception e) 
{
/*check if output path exists - error*/
System.out.println("output path does not exist");
System.exit(0);
}	
}
public void generatePMT(PCB pcb)
{
int memoryInputFrames=0;
int memoryInputWordLength=0;
int memoryOutputFrames=0;
int memoryOutputWordLength=0;
int memoryProgramFrames=0;
int memoryProgramWordLength=0;
try
{
outputBufferedWriter.write("Page Map Table of Job   :\t"+pcb.jobId+"(DECIMAL)");
outputBufferedWriter.newLine();
outputBufferedWriter.write("Input-Data Segment      :\t"+pcb.inputDataSegmentInformation);
outputBufferedWriter.newLine();
outputBufferedWriter.write("Page Map Table for Input-Data Segment and Input Data Segment :");
if(LOADER.pageTables.get(pcb.inputDataSegmentInformation)!=null)
{
for(Entry<Integer, List<Integer>> entry: LOADER.pageTables.get(pcb.inputDataSegmentInformation).entrySet())
{
outputBufferedWriter.newLine();
outputBufferedWriter.write("Page "+entry.getKey()+"-frame "+entry.getValue().get(0));
outputBufferedWriter.newLine();
for(int i=0;i<8;i++)
{
int memoryAddress=entry.getValue().get(0)*8+i;
outputBufferedWriter.write(MEMORY.mainMemory[memoryAddress]);
outputBufferedWriter.write(" ");
}
memoryInputFrames+=1;
memoryInputWordLength+=(entry.getValue().get(3)/4);
}
}
outputBufferedWriter.newLine();
outputBufferedWriter.write("Output-Data Segment     :\t"+pcb.outputDataSegmentInformation);
outputBufferedWriter.newLine();
outputBufferedWriter.write("Page Map Table for Output-Data Segment and Output Data Segment :");
if(LOADER.pageTables.get(pcb.outputDataSegmentInformation)!=null)
{
for(Entry<Integer, List<Integer>> entry: LOADER.pageTables.get(pcb.outputDataSegmentInformation).entrySet())
{
outputBufferedWriter.newLine();
outputBufferedWriter.write("Page "+entry.getKey()+"-frame "+entry.getValue().get(0));
outputBufferedWriter.newLine();
for(int i=0;i<8;i++)
{
int memoryAddress=entry.getValue().get(0)*8+i;
outputBufferedWriter.write(MEMORY.mainMemory[memoryAddress]);
outputBufferedWriter.write(" ");
}
memoryOutputFrames+=1;
memoryOutputWordLength+=(entry.getValue().get(3)/4);
}
}
outputBufferedWriter.newLine();
outputBufferedWriter.write("Page Map Table for Program Segment    :\t");
outputBufferedWriter.newLine();
if(LOADER.pageTables.get(pcb.programSegmentInformation)!=null)
{
for(Entry<Integer, List<Integer>> entry: LOADER.pageTables.get(pcb.programSegmentInformation).entrySet())
{
outputBufferedWriter.write("Page "+entry.getKey()+"-frame "+entry.getValue().get(0));
outputBufferedWriter.newLine();
memoryProgramFrames+=1;
memoryProgramWordLength+=(entry.getValue().get(3)/4);
}
}
int totalMemoryFrames=memoryInputFrames+memoryOutputFrames+memoryProgramFrames;
int totalMemoryWordLength=memoryInputWordLength+memoryOutputWordLength+memoryProgramWordLength;
int totalMemoryUtilization=(totalMemoryWordLength*100)/256;
SYSTEM.meanMemoryUtilization+=totalMemoryUtilization;
outputBufferedWriter.write("Memory utilization both as a ratio and as a percentage :");
outputBufferedWriter.newLine();
outputBufferedWriter.write(totalMemoryWordLength+"/256 words "+totalMemoryUtilization+" %");
outputBufferedWriter.newLine();
outputBufferedWriter.write(totalMemoryFrames+"/32 frames "+((totalMemoryFrames*100)/32)+" %");
outputBufferedWriter.newLine();
int diskInputFrames=0;
int diskInputWordLength=0;
int diskOutputFrames=0;
int diskOutputWordLength=0;
int diskProgramFrames=0;
int diskProgramWordLength=0;
if(DISK.diskManager.get(pcb.inputDataSegmentInformation)!=null)
{
for(Entry<Integer, List<Integer>> entry: DISK.diskManager.get(pcb.inputDataSegmentInformation).entrySet())
{
diskInputFrames+=1;
diskInputWordLength+=entry.getValue().get(1)/4;
}
}
if(DISK.diskManager.get(pcb.outputDataSegmentInformation)!=null)
{
for(Entry<Integer, List<Integer>> entry: DISK.diskManager.get(pcb.outputDataSegmentInformation).entrySet())
{
diskOutputFrames+=1;
diskOutputWordLength+=(entry.getValue().get(1)/4);
}
}
if(DISK.diskManager.get(pcb.programSegmentInformation)!=null)
{
for(Entry<Integer, List<Integer>> entry: DISK.diskManager.get(pcb.programSegmentInformation).entrySet())
{
diskProgramFrames+=1;
diskProgramWordLength+=(entry.getValue().get(1)/4);
}
}
int totalDiskFrames=diskInputFrames+diskOutputFrames+diskProgramFrames;
int totalDiskWordLength=diskInputWordLength+diskOutputWordLength+diskProgramWordLength;
int totalDiskUtilization=(totalDiskWordLength*100)/2048;
SYSTEM.meanDiskUtilization+=totalDiskUtilization;
outputBufferedWriter.write("Disk utilization both as a ratio and as a percentage :");
outputBufferedWriter.newLine();
outputBufferedWriter.write(totalDiskWordLength+"/2048 words "+totalDiskUtilization+" %");
outputBufferedWriter.newLine();
outputBufferedWriter.write(totalDiskFrames+"/256 frames "+((totalDiskFrames*100)/256)+" %");
outputBufferedWriter.newLine();
}
catch(Exception e)
{
/*check if output path exists - error*/
System.out.println("output path does not exist");
System.exit(0);
}
}
public void generateJobOutputFile(PCB pcb)
{
try
{
if(pcb.errorMessage!="")
{
outputBufferedWriter.write("Warning Message         :\t"+pcb.errorMessage);
outputBufferedWriter.newLine();
}
outputBufferedWriter.write("Job Termination         :\t"+pcb.terminatingMessage);
outputBufferedWriter.newLine();
outputBufferedWriter.write("Number of Page Faults   :\t"+pcb.numberOfPageFaults);
outputBufferedWriter.newLine();
outputBufferedWriter.write("System Clock            :\t"+Integer.toHexString(CPU.clock).toUpperCase()+"(HEX)");
outputBufferedWriter.newLine();
outputBufferedWriter.write("Arrival Time            :\t"+Integer.toHexString(pcb.arrivalTime).toUpperCase()+"(HEX)");
outputBufferedWriter.newLine();
outputBufferedWriter.write("Input/Output time       :\t"+pcb.iotime+"(DECIMAL)");
outputBufferedWriter.newLine();
outputBufferedWriter.write("Execution time          :\t"+(pcb.clock-(pcb.iotime+pcb.segmentFaultTime+pcb.pageFaultTime))+"(DECIMAL)");
outputBufferedWriter.newLine();
outputBufferedWriter.write("Page Fault time         :\t"+pcb.pageFaultTime+"(DECIMAL)");
outputBufferedWriter.newLine();
outputBufferedWriter.write("Segment Fault time      :\t"+pcb.segmentFaultTime
+"(DECIMAL)");
outputBufferedWriter.newLine();
outputBufferedWriter.write("Departure time          :\t"+Integer.toHexString(pcb.departureTime).toUpperCase()+"(HEX)");
outputBufferedWriter.newLine();
outputBufferedWriter.write("Turn-Around time        :\t"+(pcb.departureTime-pcb.arrivalTime)+"(DECIMAL)");
outputBufferedWriter.newLine(); 
}
catch(Exception e)
{
/*check if output path exists - error*/ 
System.out.println("output path does not exist");
System.exit(0);
}
}
public void writeTrace(PCB pcb)
{
if(pcb.traceSwitch.equals("1"))
{
try
{
traceFile=new File("C:\\Users\\welcome\\Desktop\\trace\\trace_file_job_"+pcb.jobIdInPacket+"_"+pcb.jobId+".txt");
if(traceFile.exists())
{
traceFile.delete();
}
traceFile.createNewFile();
fileWriter=new FileWriter(traceFile,true);
traceWriter=new BufferedWriter(fileWriter); 
traceWriter.write(String.format("%s%6s%6s%7s%6s%4s%7s%6s%6s%4s%7s",
"PC","BR","IR","TOS"," S[TOS]","EA","(EA)","TOS"," S[TOS]","EA","(EA)"));
traceWriter.newLine();
traceWriter.write(String.format("%s%6s%6s%6s%6s%6s%6s%6s%6s%6s%6s",
"HEX","HEX","HEX","HEX","HEX","HEX","HEX","HEX","HEX","HEX","HEX"));
traceWriter.newLine();
}
catch(Exception e)
{
/*check if the output path exists - error*/
System.out.println("output path does not exist");
System.exit(0);
}
}
}
public void clearMemoryAndDiskJobFrames(PCB pcb)
{
List<Integer> diskPageFrames=new ArrayList<Integer>();
for(int i=0;i<pcb.jobPageFrames.length;i++)
{
for(int j=0;j<8;j++)
{
int memoryAddress=pcb.jobPageFrames[i]*8+j;
char empty='\0';
for(int k=0;k<16;k++)
{
MEMORY.mainMemory[memoryAddress][k]=empty;
}
}
MEMORY.freeMemoryBitVector[pcb.jobPageFrames[i]]=0;
}
if(pcb.programSegmentInformation!=null)
{
if(DISK.diskManager.get(pcb.programSegmentInformation)!=null)
{
for(Entry<Integer, List<Integer>> entry: DISK.diskManager.get(pcb.programSegmentInformation).entrySet())
{
diskPageFrames.add(entry.getValue().get(0));
}
LOADER.pageTables.remove(pcb.programSegmentInformation);
DISK.diskManager.remove(pcb.programSegmentInformation);
}
}
if(pcb.inputDataSegmentInformation!=null)
{
for(Entry<Integer, List<Integer>> entry: DISK.diskManager.get(pcb.inputDataSegmentInformation).entrySet())
{
diskPageFrames.add(entry.getValue().get(0));
}
LOADER.pageTables.remove(pcb.inputDataSegmentInformation);
DISK.diskManager.remove(pcb.inputDataSegmentInformation);
}
if(pcb.outputDataSegmentInformation!=null)
{
for(Entry<Integer, List<Integer>> entry: DISK.diskManager.get(pcb.outputDataSegmentInformation).entrySet())
{
diskPageFrames.add(entry.getValue().get(0));
}
LOADER.pageTables.remove(pcb.outputDataSegmentInformation);
DISK.diskManager.remove(pcb.outputDataSegmentInformation);
}
if(diskPageFrames.size()>0)
{
for(int i=0;i<diskPageFrames.size();i++)
{
for(int j=0;j<8;j++)
{
int diskAddress=(diskPageFrames.get(i))*8+j;
char empty='\0';
for(int k=0;k<16;k++)
{
DISK.virtualMemory[diskAddress][k]=empty;
}
}
DISK.freeDiskBitVector[diskPageFrames.get(i)]=0;
}
}
}
}