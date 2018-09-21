/*
CPU class :
cpu() method takes a job in ready queue and keeps looping indefinitely 
executing all instructions until it encounters a HLT instruction or an error.
twoComplementDecimalValue() returns the decimal value for the given two's 
complement binary value.
getStackValue() method is used to get the stack value at a particular 
index in stack.
saveToStack() method is used to save the value to stack at a particular
index in stack.
zeroPadding() method is used to convert every value to a 16 bit binary 
representation.
type0() and type1() methods are used to process respective instruction 
types.
*/
/*
GLOBAL VARIABLES :
'cpuStack' is the stack in CPU which is a two 
dimensional array of size 7 x 16.
'topOfStack' is a pointer to point to the top of the stack.
'traceContent' to hold the data to be written into trace file.
'fileWriter' to write into trace file.
'traceWriter is the buffered writer to write data into the trace file.
'instructionRegister' to hold the current instruction that is being 
executed.
'baseRegister' to hold the starting address in memory where the 
instructions have been loaded.
'valueInEffectiveAddress' to hold the value present in the effective address.
'programCounter' to hold the address of the next instruction to be executed.
'effective address is the memory address where data is present.
'trace' to hold trace bit.
'tosValue' to hold the value in top of the stack.
*/
package Vasanthi_Mudunuri_OS_Phase2;

import java.io.IOException;

public class CPU 
{
public static char[][] cpuStack=new char[7][16];
public static String traceContent;
public static String instructionRegister;
public static String valueInEffectiveAddress;
public static int programCounter;
public static int effectiveAddress;
public static String tosValue;
public static String trace;
public static int clock;
public static int topOfStack;
public static boolean breakFromCPU=false;
public static boolean haltFlag = false;
public static boolean errorFlag=false;
public static int terminatedNormalJobCount=0;
public static int samplingInterval=0;
public static int cpuCount=0;

public static void cpu(String pc,String traceSwitch) throws IOException
{
PCB pcb=SCHEDULER.readyQueue.element();
cpuStack=pcb.pcbStack;
programCounter=Integer.parseInt(pc,16);
topOfStack=pcb.topOfStack;
trace=traceSwitch;
pcb.generateJobDetails(pcb);
while(true && !(breakFromCPU) && !haltFlag && !errorFlag)
{
instructionRegister=MEMORY.memory(pcb,"READ",pcb.programSegmentInformation,programCounter,instructionRegister);
if(traceSwitch.equals("1"))
{
String content=Integer.toHexString(programCounter).toUpperCase();
while(content.length()<2)
{
content="0"+content;
}
String content1=Integer.toHexString(Integer.parseInt(instructionRegister,2))
.toUpperCase();
while(content1.length()<4)
{
content1="0"+content1;
}
traceContent=content+" "+
Integer.toString(pcb.startingAddress).toUpperCase()+" "+
content1;
}
if((pcb.clock-(pcb.iotime+pcb.segmentFaultTime+pcb.pageFaultTime))>10000)
{
/*check if the execution entered into infinite loop - error*/
ERROR_HANDLER.Error_Handler(15);
SCHEDULER.readyQueue.remove();
errorFlag=true;
break;
}
programCounter+=1;
if(instructionRegister.charAt(0)=='1')
{
clock+=4;
pcb.clockForCpu+=4;
pcb.clockToWriteOutput+=4;
pcb.clock+=4;
pcb.burstTime+=4;
type1(pcb,instructionRegister);
}
else if(instructionRegister.charAt(0)=='0' 
&& instructionRegister.charAt(8)=='0' && 
(Integer.parseInt(instructionRegister.substring(0,8),2))!=0)
{
if(Integer.parseInt(instructionRegister.substring(0,8),2)!=15)
{
clock+=2;
pcb.clockForCpu+=2;
pcb.clockToWriteOutput+=2;
pcb.clock+=2;
pcb.burstTime+=2;
type0(pcb,"00000000"+instructionRegister.substring(0,8));
String content=Integer.toHexString(programCounter).toUpperCase();
while(content.length()<2)
{
content="0"+content;
}
String content1=Integer.toHexString(Integer.parseInt(instructionRegister,2))
.toUpperCase();
while(content1.length()<4)
{
content1="0"+content1;
}
traceContent=content+" "+
Integer.toString(pcb.startingAddress).toUpperCase()+" "+content1;
type0(pcb,"00000000"+instructionRegister.substring(8,16));
}
else
{
clock+=1;
pcb.clockForCpu+=1;
pcb.clockToWriteOutput+=1;
pcb.clock+=1;
pcb.burstTime+=1;
type0(pcb,"00000000"+instructionRegister.substring(0,8));
String content=Integer.toHexString(programCounter).toUpperCase();
while(content.length()<2)
{
content="0"+content;
}
String content1=Integer.toHexString(Integer.parseInt(instructionRegister,2))
.toUpperCase();
while(content1.length()<4)
{
content1="0"+content1;
}
traceContent=content+" "+
Integer.toString(pcb.startingAddress).toUpperCase()+" "+content1;
type0(pcb,"00000000"+instructionRegister.substring(8,16));
}
}
else
{
clock+=2; 
pcb.clockForCpu+=2;
pcb.clockToWriteOutput+=1;
pcb.clock+=2;
pcb.burstTime+=2;
type0(pcb,instructionRegister);
}
if(pcb.clockToWriteOutput>15)
{
pcb.generatePMT(pcb);
pcb.clockToWriteOutput=0;
samplingInterval+=1;
}
if(pcb.clockForCpu>=20 && !haltFlag && !errorFlag)
{
pcb.clockForCpu=0;
pcb.topOfStack=topOfStack;
for(int i=0;i<cpuStack.length;i++)
{
for(int j=0;j<16;j++)
{
pcb.pcbStack[i][j]=cpuStack[i][j];
}
}
pcb.initialProgramCounter=Integer.toHexString(programCounter);
SCHEDULER.readyForReadyQueue.add(pcb);
SCHEDULER.readyQueue.remove();
breakFromCPU=true;
}
}
}
public static int twoComplementDecimalValue(String binary)
{
if(binary.charAt(0)=='1' && binary.length()==16)
{
binary=binary.replace('0','#');
binary=binary.replace('1','0');
binary=binary.replace('#','1');
int decimalValue=(Integer.parseInt(binary,2)+1)*-1;
return decimalValue;
}
return Integer.parseInt(binary,2);
}
public static String getStackValue(int index)
{
String value="";
for(int i=0;i<16;i++)
{
value=value+cpuStack[index][i];
}
return value;
}
public static void saveToStack(int index,String value)
{
for(int k=0;k<16;k++)
{
cpuStack[index][k]=value.charAt(k);
}
}
public static String zeroPadding(String binary)
{
int length=binary.length();
if(length!=16)
{
for(int j=0;j<16-length;j++)
{
binary="0"+binary;
}
}
return binary;
}
public static void type0(PCB pcb,String instruction) throws IOException
{ 
String opcode=instruction.substring(11,16);
String belowtosValue;
String Result;
if(trace.equals("1"))
{
if(topOfStack==-1)
{
tosValue="0";
}
else
{
tosValue=getStackValue(topOfStack);
}
String content1=Integer.toHexString(Integer.parseInt(tosValue,2)).toUpperCase();
while(content1.length()<4)
{
content1="0"+content1;
}
if(topOfStack==-1)
{
traceContent=traceContent+" "+"-1"+" "+
content1;
}
else
{
traceContent=traceContent+" "+Integer.toHexString(topOfStack)
.toUpperCase()+" "+content1;
}
}
switch(opcode)
{ 
case "00000":break;
case "00001":tosValue=getStackValue(topOfStack);
belowtosValue=getStackValue(topOfStack-1);
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(tosValue)|twoComplementDecimalValue(belowtosValue)));
saveToStack(topOfStack-1,Result);
topOfStack-=1;
break;
case "00010":tosValue=getStackValue(topOfStack);
belowtosValue=getStackValue(topOfStack-1);
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(tosValue)&twoComplementDecimalValue(belowtosValue)));
saveToStack(topOfStack-1,Result);
topOfStack-=1;
break;
case "00011":tosValue=getStackValue(topOfStack);
Result=zeroPadding(Integer.toBinaryString(~(twoComplementDecimalValue(tosValue))));
saveToStack(topOfStack,Result);
break;
case "00100":tosValue=getStackValue(topOfStack); 
belowtosValue=getStackValue(topOfStack-1);
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(tosValue)^twoComplementDecimalValue(belowtosValue)));
saveToStack(topOfStack-1,Result);
topOfStack-=1;
break;
case "00101":tosValue=getStackValue(topOfStack);
belowtosValue=getStackValue(topOfStack-1);
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(tosValue)+twoComplementDecimalValue(belowtosValue)));
saveToStack(topOfStack-1,Result);
topOfStack-=1;
break;
case "00110":tosValue=getStackValue(topOfStack);
belowtosValue=getStackValue(topOfStack-1);
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(belowtosValue)-twoComplementDecimalValue(tosValue)));
saveToStack(topOfStack-1,Result);
topOfStack-=1;
break;
case "00111":tosValue=getStackValue(topOfStack);
belowtosValue=getStackValue(topOfStack-1);
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(tosValue)*twoComplementDecimalValue(belowtosValue)));
saveToStack(topOfStack-1,Result);
topOfStack-=1;
break;
case "01000":tosValue=getStackValue(topOfStack);
belowtosValue=getStackValue(topOfStack-1);
if(twoComplementDecimalValue(tosValue)==0)
{
/*Divide by zero - error*/
ERROR_HANDLER.Error_Handler(27);
SCHEDULER.readyQueue.remove();
errorFlag=true;
break;
}
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(belowtosValue)/twoComplementDecimalValue(tosValue)));
saveToStack(topOfStack-1,Result);
topOfStack-=1;
break;
case "01001":tosValue=getStackValue(topOfStack);
belowtosValue=getStackValue(topOfStack-1);
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(belowtosValue)%twoComplementDecimalValue(tosValue)));
saveToStack(topOfStack-1,Result);
topOfStack-=1;
break;
case "01010":tosValue=getStackValue(topOfStack);
Result=zeroPadding(tosValue.substring(1,16)+"0");
saveToStack(topOfStack,Result);
break;
case "01011":tosValue=getStackValue(topOfStack);
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(tosValue) >>> 1));
saveToStack(topOfStack,Result);
break;
case "01100":tosValue=getStackValue(topOfStack);
belowtosValue=getStackValue(topOfStack-1);
if(twoComplementDecimalValue(belowtosValue)
>twoComplementDecimalValue(tosValue))
{
saveToStack(topOfStack+1,"0000000000000001");                     
}
else
{
saveToStack(topOfStack+1,"0000000000000000");  
}
topOfStack+=1;
break;
case "01101":tosValue=getStackValue(topOfStack);
belowtosValue=getStackValue(topOfStack-1);
if(twoComplementDecimalValue(belowtosValue)
<twoComplementDecimalValue(tosValue))
{
saveToStack(topOfStack+1,"0000000000000001");  
}
else
{
saveToStack(topOfStack+1,"0000000000000000");  
}
topOfStack+=1;
break;
case "01110":tosValue=getStackValue(topOfStack);
belowtosValue=getStackValue(topOfStack-1);
if(twoComplementDecimalValue(belowtosValue)
==twoComplementDecimalValue(tosValue))
{
saveToStack(topOfStack+1,"0000000000000001");  
}
else
{
saveToStack(topOfStack+1,"0000000000000000");  
}
topOfStack+=1;
break;
case "01111":break;
case "10000":break;
case "10001":break;
case "10010":break;
case "10011":
pcb.clock+=15;
pcb.iotime+=15; 
pcb.inputCount+=1;
if(pcb.inputCount>pcb.inputDataSegmentSize)
{
/*check if reading beyond the end of file - error*/
ERROR_HANDLER.Error_Handler(28);
SCHEDULER.readyQueue.remove();
errorFlag=true;
break;
}
topOfStack+=1;
try
{
int input=twoComplementDecimalValue(MEMORY.readInputFromMemory(pcb,pcb.inputDataSegmentInformation));
if(input<-8192 || input>8191)
{
/*check if integer value is out of range - error*/
ERROR_HANDLER.Error_Handler(2);
SCHEDULER.readyQueue.remove();
errorFlag=true;
break;
}
else
{
if(input>=0)
{
saveToStack(topOfStack,zeroPadding(Integer.toBinaryString(input)));
}
else
{
String twoComplement=Integer.toBinaryString(input);
saveToStack(topOfStack,twoComplement.substring
(twoComplement.length()-16,twoComplement.length()));
}
}
}
catch(Exception e)
{
/*check if input is not an integer - error*/
ERROR_HANDLER.Error_Handler(3);
SCHEDULER.readyQueue.remove();
errorFlag=true;
break;
}
break;
case "10100":
pcb.clock+=15;
pcb.iotime+=15;
pcb.outputCount+=1;
if(pcb.outputCount>pcb.outputDataSegmentSize)
{
/*check if writing beyond the end of file - error*/
ERROR_HANDLER.Error_Handler(29);
SCHEDULER.readyQueue.remove();
errorFlag=true;
break;
}
MEMORY.writeOutputToMemory(pcb,pcb.outputDataSegmentInformation,getStackValue(topOfStack));
topOfStack-=1;
break;
case "10101":programCounter=Integer.parseInt(getStackValue(topOfStack),2);
topOfStack-=1;
break;
case "10110":break;
case "10111":break;
case "11000":pcb.generatePMT(pcb);
pcb.departureTime=CPU.clock;
SYSTEM.meanTurnAroundTime+=(pcb.departureTime-pcb.arrivalTime);
SYSTEM.meanWaitingTime+=((pcb.departureTime-pcb.arrivalTime))-pcb.burstTime;
SYSTEM.meanPageFaults+=pcb.numberOfPageFaults;
SYSTEM.cpuTime[cpuCount]=pcb.burstTime;
SYSTEM.jobTurnAroundTime[cpuCount]=(pcb.departureTime-pcb.arrivalTime);
cpuCount++;
pcb.generateJobOutputFile(pcb);
if(trace.equals("1"))
{
if(topOfStack==-1)
{
tosValue="0";
}
else
{
tosValue=getStackValue(topOfStack);
}
String[] traceOut=traceContent.split("\\s+");
for(String s : traceOut)
{
pcb.traceWriter.write(String.format("%-6s",s));
}
String content=Integer.toHexString(Integer.parseInt(tosValue,2)).toUpperCase();
while(content.length()<4)
{
content="0"+content;
}
if(topOfStack==-1)
{
traceContent=String.format("%13s     %s",-1,
content);	
}
else
{
traceContent=String.format("%13s     %s",Integer.toHexString(topOfStack).toUpperCase(),
content);
}
pcb.traceWriter.write(traceContent);
pcb.traceWriter.newLine();
//pcb.traceWriter.close();
}
terminatedNormalJobCount+=1;
pcb.clearMemoryAndDiskJobFrames(pcb);
SCHEDULER.readyQueue.remove();
haltFlag = true;
break;
default:/*check for invalid opcode - error*/
ERROR_HANDLER.Error_Handler(14);
SCHEDULER.readyQueue.remove();
SYSTEM.skipErrorJob(pcb);
break;
}
if(trace.equals("1"))
{
if(topOfStack==-1)
{
tosValue="0";
}
else
{
tosValue=getStackValue(topOfStack);
}
String[] traceOut=traceContent.split("\\s+");
for(String s : traceOut)
{
pcb.traceWriter.write(String.format("%-6s",s));
}
String content=Integer.toHexString(Integer.parseInt(tosValue,2)).toUpperCase();
while(content.length()<4)
{
content="0"+content;
}
if(topOfStack==-1)
{
traceContent=String.format("%13s     %s",-1,
content);	
}
else
{
traceContent=String.format("%13s     %s",Integer.toHexString(topOfStack).toUpperCase(),
content);
}
pcb.traceWriter.write(traceContent);
pcb.traceWriter.newLine();
}
}
public static void type1(PCB pcb,String instruction) throws IOException
{
String opcode=instruction.substring(1,6);
char index=instruction.charAt(6);
int displacementAddress=Integer.parseInt(instruction.substring(9,16),2);
String memoryValue;
String Result;
if(index=='1')
{
effectiveAddress=displacementAddress+Integer.parseInt(getStackValue(topOfStack),2);
}
else
{
effectiveAddress=displacementAddress;
}
if(effectiveAddress>MEMORY.mainMemory.length || effectiveAddress<0)
{
/*check for invalid effective address - error*/
ERROR_HANDLER.Error_Handler(18);
SCHEDULER.readyQueue.remove();
SYSTEM.skipErrorJob(pcb);
}
if(trace.equals("1"))
{
if(topOfStack==-1)
{
tosValue="0";
}
else
{
tosValue=getStackValue(topOfStack);
}
memoryValue=MEMORY.memory(pcb,"READ",pcb.programSegmentInformation,effectiveAddress,valueInEffectiveAddress);
String content=Integer.toHexString(Integer.parseInt(tosValue,2)).toUpperCase();
while(content.length()<4)
{
content="0"+content;
}
String content1=Integer.toHexString(effectiveAddress).toUpperCase();
while(content1.length()<2)
{
content1="0"+content1;
}
String content2=Integer.toHexString(Integer.parseInt(memoryValue,2)).toUpperCase();
while(content2.length()<4)
{
content2="0"+content2;
}
if(topOfStack==-1)
{
traceContent=traceContent+" "+"-1"+" "+
content+" "+
content1+" "+content2;
}
else
{
traceContent=traceContent+" "+Integer.toHexString(topOfStack).toUpperCase()+" "+
content+" "+
content1+" "+content2;
}
}
switch(opcode)
{
case "00000":break;
case "00001":memoryValue=MEMORY.memory(pcb,"READ",pcb.programSegmentInformation,effectiveAddress,valueInEffectiveAddress);
tosValue=getStackValue(topOfStack);
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(tosValue)|twoComplementDecimalValue(memoryValue)));
saveToStack(topOfStack,Result);
break;  
case "00010":memoryValue=MEMORY.memory(pcb,"READ",pcb.programSegmentInformation,effectiveAddress,valueInEffectiveAddress);
tosValue=getStackValue(topOfStack);
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(tosValue)&twoComplementDecimalValue(memoryValue)));
saveToStack(topOfStack,Result);
break;
case "00011":break;
case "00100":memoryValue=MEMORY.memory(pcb,"READ",pcb.programSegmentInformation,effectiveAddress,valueInEffectiveAddress);
tosValue=getStackValue(topOfStack);
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(tosValue)^twoComplementDecimalValue(memoryValue)));
saveToStack(topOfStack,Result);
break;
case "00101":memoryValue=MEMORY.memory(pcb,"READ",pcb.programSegmentInformation,effectiveAddress,valueInEffectiveAddress);
tosValue=getStackValue(topOfStack);
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(tosValue)+twoComplementDecimalValue(memoryValue)));
saveToStack(topOfStack,Result);
break;	
case "00110":memoryValue=MEMORY.memory(pcb,"READ",pcb.programSegmentInformation,effectiveAddress,valueInEffectiveAddress);
tosValue=getStackValue(topOfStack);
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(tosValue)-twoComplementDecimalValue(memoryValue)));
saveToStack(topOfStack,Result);
break;
case "00111":memoryValue=MEMORY.memory(pcb,"READ",pcb.programSegmentInformation,effectiveAddress,valueInEffectiveAddress);
tosValue=getStackValue(topOfStack);
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(tosValue)*twoComplementDecimalValue(memoryValue)));
saveToStack(topOfStack,Result);
break; 
case "01000":memoryValue=MEMORY.memory(pcb,"READ",pcb.programSegmentInformation,effectiveAddress,valueInEffectiveAddress);
tosValue=getStackValue(topOfStack);
if(twoComplementDecimalValue(memoryValue)==0)
{
/*Divide by zero - error*/
ERROR_HANDLER.Error_Handler(27);
SCHEDULER.readyQueue.remove();
errorFlag=true;
break;
}
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(tosValue)/twoComplementDecimalValue(memoryValue)));
saveToStack(topOfStack,Result);
break; 
case "01001":memoryValue=MEMORY.memory(pcb,"READ",pcb.programSegmentInformation,effectiveAddress,valueInEffectiveAddress);
tosValue=getStackValue(topOfStack);
Result=zeroPadding(Integer.toBinaryString
(twoComplementDecimalValue(tosValue)%twoComplementDecimalValue(memoryValue)));
saveToStack(topOfStack,Result);
break; 
case "01010":break;
case "01011":break;
case "01100":memoryValue=MEMORY.memory(pcb,"READ",pcb.programSegmentInformation,effectiveAddress,valueInEffectiveAddress);
tosValue=getStackValue(topOfStack);
/*True value is assumed to be 1 and false value to 0*/
if(twoComplementDecimalValue(tosValue)>twoComplementDecimalValue(memoryValue))
{
saveToStack(topOfStack+1,"0000000000000001");
}
else
{
saveToStack(topOfStack+1,"0000000000000000");
}
topOfStack+=1;
break;
case "01101":memoryValue=MEMORY.memory(pcb,"READ",pcb.programSegmentInformation,effectiveAddress,valueInEffectiveAddress);
tosValue=getStackValue(topOfStack);
if(twoComplementDecimalValue(tosValue)<twoComplementDecimalValue(memoryValue))
{
saveToStack(topOfStack+1,"0000000000000001");
}
else
{
saveToStack(topOfStack+1,"0000000000000000");
}
topOfStack+=1;
break;
case "01110":memoryValue=MEMORY.memory(pcb,"READ",pcb.programSegmentInformation,effectiveAddress,valueInEffectiveAddress);
tosValue=getStackValue(topOfStack);
if(twoComplementDecimalValue(tosValue)==twoComplementDecimalValue(memoryValue))
{
saveToStack(topOfStack+1,"0000000000000001");
}
else
{
saveToStack(topOfStack+1,"0000000000000000");
}
topOfStack+=1;
break;
case "01111":programCounter=effectiveAddress;
break;
case "10000":if(getStackValue(topOfStack).equals("0000000000000001"))
{
programCounter=effectiveAddress;
}
topOfStack-=1;
break;
case "10001":if(getStackValue(topOfStack).equals("0000000000000000"))
{
programCounter=effectiveAddress;
}
topOfStack-=1;
break;
case "10010":topOfStack+=1;
saveToStack(topOfStack,zeroPadding(Integer.toBinaryString(programCounter)));
programCounter=effectiveAddress;
break;
case "10011":break;
case "10100":break;
case "10101":break;
case "10110": topOfStack+=1;
if(topOfStack==7)
{
/*stack overflow - error*/
ERROR_HANDLER.Error_Handler(17);
SCHEDULER.readyQueue.remove();
errorFlag=true;
break;
}
memoryValue=MEMORY.memory(pcb,"READ",pcb.programSegmentInformation,effectiveAddress,valueInEffectiveAddress);
saveToStack(topOfStack,zeroPadding(memoryValue));
break;
case "10111":if(topOfStack==-1)
{
/*stack underflow - error*/
ERROR_HANDLER.Error_Handler(16);
SCHEDULER.readyQueue.remove();
errorFlag=true;
break;
}
else
{
tosValue=getStackValue(topOfStack);
int length=tosValue.length();
if(length!=16)
{
for(int i=0;i<16-length;i++)
{
 tosValue="0"+tosValue;
}
}
MEMORY.memory(pcb,"WRITE",pcb.programSegmentInformation,effectiveAddress,tosValue);
topOfStack-=1;
}
break;
case "11000":break; 
default:/*check for invalid opcode - error*/
ERROR_HANDLER.Error_Handler(14);
SCHEDULER.readyQueue.remove();
SYSTEM.skipErrorJob(pcb);
break;
}
if(trace.equals("1"))
{
if(topOfStack==-1)
{
tosValue="0";
}
else
{
tosValue=getStackValue(topOfStack);
}
memoryValue=MEMORY.memory(pcb,"READ",pcb.programSegmentInformation,effectiveAddress,valueInEffectiveAddress);
String content=Integer.toHexString(Integer.parseInt(tosValue,2)).toUpperCase();
while(content.length()<4)
{
content="0"+content;
}
String content1=Integer.toHexString(effectiveAddress).toUpperCase();
while(content1.length()<2)
{
content1="0"+content1;
}
String content2=Integer.toHexString(Integer.parseInt(memoryValue,2)).toUpperCase();
while(content2.length()<4)
{
content2="0"+content2;
}
if(topOfStack==-1)
{
traceContent=traceContent+" "+"-1"+" "+
content+" "+
content1+" "+content2;
}
else
{
traceContent=traceContent+" "+Integer.toHexString(topOfStack).toUpperCase()+" "+
content+" "+
content1+" "+content2;
}
String[] traceOut=traceContent.split("\\s+");
for(String s : traceOut)
{
pcb.traceWriter.write(String.format("%-6s",s));
}
pcb.traceWriter.newLine();
}
}
}