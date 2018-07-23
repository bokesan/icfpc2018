package org.astormofminds.icfpc2018;

import org.astormofminds.icfpc2018.io.Binary;
import org.astormofminds.icfpc2018.model.Command;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Combine a destructor and assembler traces to reconstructor traces.
 */
public class Combine {

    private static final String[][] COMBS = {
            {"FR001","FD007","FA004"},
            {"FR002","FD004","FA008"},
            {"FR003","FD008","FA007"},
            {"FR004","FD011","FA012"},
            {"FR005","FD012","FA013"},
            {"FR006","FD013","FA014"},
            {"FR007","FD014","FA011"},
            {"FR008","FD015","FA016"},
            {"FR009","FD016","FA017"},
            {"FR010","FD017","FA018"},
            {"FR011","FD018","FA015"},
            {"FR012","FD021","FA019"},
            {"FR013","FD019","FA021"},
            {"FR020","FD027","FA029"},
            {"FR021","FD029","FA027"},
            {"FR022","FD028","FA030"},
            {"FR023","FD030","FA028"},
            {"FR027","FD025","FA037"},
            {"FR028","FD039","FA023"},
            {"FR031","FD031","FA032"},
            {"FR032","FD032","FA031"},
            {"FR037","FD042","FA043"},
            {"FR038","FD043","FA042"},
            {"FR041","FD044","FA045"},
            {"FR043","FD045","FA044"},
            {"FR045","FD057","FA058"},
            {"FR046","FD058","FA057"},
            {"FR049","FD059","FA060"},
            {"FR050","FD060","FA059"},
            {"FR053","FD062","FA064"},
            {"FR054","FD072","FA073"},
            {"FR055","FD073","FA074"},
            {"FR056","FD074","FA075"},
            {"FR057","FD075","FA076"},
            {"FR058","FD076","FA077"},
            {"FR059","FD077","FA072"},
            {"FR060","FD078","FA079"},
            {"FR061","FD079","FA080"},
            {"FR062","FD080","FA081"},
            {"FR063","FD081","FA082"},
            {"FR064","FD082","FA078"},
            {"FR065","FD084","FA083"},
            {"FR068","FD102","FA103"},
            {"FR069","FD103","FA102"},
            {"FR071","FD107","FA111"},
            {"FR072","FD110","FA108"},
            {"FR073","FD108","FA109"},
            {"FR078","FD129","FA130"},
            {"FR079","FD130","FA131"},
            {"FR080","FD131","FA132"},
            {"FR081","FD132","FA133"},
            {"FR082","FD133","FA134"},
            {"FR083","FD134","FA135"},
            {"FR084","FD135","FA136"},
            {"FR087","FD137","FA138"},
            {"FR091","FD155","FA156"},
            {"FR092","FD156","FA155"},
            {"FR095","FD144","FA145"},
            {"FR096","FD145","FA146"},
            {"FR097","FD146","FA144"},
            {"FR099","FD157","FA158"},
            {"FR100","FD164","FA165"},
            {"FR104","FD171","FA172"},
            {"FR105","FD178","FA179"},
            {"FR107","FD179","FA178"},
            {"FR111","FD181","FA185"},
            {"FR112","FD183","FA184"}
    };

    private final File[] traces;

    public Combine(File traceFolder) {
        traces = traceFolder.listFiles((dir, name) -> name.endsWith(".nbt"));
    }

    private File exists(String name) {
        for (File f : traces) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    public void combineAll() throws IOException {
        for (String[] recon : COMBS) {
            combine(recon[0] + ".nbt", recon[1] + ".nbt", recon[2] + ".nbt");
        }
    }

    private void combine(String dest, String decon, String asm) throws IOException {
        File destFile = exists(dest);
        if (destFile != null) {
            System.out.println(dest + " already exists");
        } else {
            File dFile = exists(decon);
            File aFile = exists(asm);
            if (dFile == null) {
                System.out.println("missing destruct trace");
            }
            else if (aFile == null) {
                System.out.println("missing assemble trace");
            }
            else {
                System.out.println("combining...");
                try (InputStream ds = new BufferedInputStream(new FileInputStream(dFile));
                     InputStream as = new BufferedInputStream(new FileInputStream(aFile)))
                {
                    List<Command> dTrace = Binary.readTrace(ds);
                    List<Command> aTrace = Binary.readTrace(as);
                    dTrace.remove(dTrace.size() - 1);
                    dTrace.addAll(aTrace);
                    String fn = dFile.getParent() + "/" + dest;
                    Binary.writeTrace(fn, dTrace);
                    System.out.print("Wrote " + fn);
                }
            }
        }
    }

}