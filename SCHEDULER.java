package Vasanthi_Mudunuri_OS_Phase2;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class SCHEDULER {
public static Queue<PCB> readyQueue=new LinkedList<PCB>();
public static Queue<PCB> readyForReadyQueue=new LinkedList<PCB>();

public static void scheduler()
{
try 
{
while(!(readyQueue.isEmpty()))
{	
PCB pcb=readyQueue.element();
if(pcb.loadFirstPageFlag==false)
{
LOADER.loader(pcb);
}
CPU.cpu(pcb.initialProgramCounter, pcb.traceSwitch);
if(CPU.breakFromCPU)
{ 
readyQueue.add(readyForReadyQueue.element());
readyForReadyQueue.remove();
}
if(CPU.haltFlag)
{
SYSTEM.loadJobInInputFile();
CPU.haltFlag=false;
}
if(CPU.errorFlag)
{
SYSTEM.skipErrorJob(pcb);
CPU.errorFlag=false;
}
CPU.breakFromCPU = false;
}
SYSTEM.meteringAndReportingFacility();
SYSTEM.saveFile();
} 
catch (IOException e) 
{
e.printStackTrace();
}
}
}
