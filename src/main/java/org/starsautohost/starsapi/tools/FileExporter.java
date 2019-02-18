package org.starsautohost.starsapi.tools;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.starsautohost.starsapi.block.Block;
import org.starsautohost.starsapi.block.BlockType;
import org.starsautohost.starsapi.block.CountersBlock;
import org.starsautohost.starsapi.block.FileHeaderBlock;
import org.starsautohost.starsapi.block.PartialPlanetBlock;
import org.starsautohost.starsapi.encryption.Decryptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FileExporter {

    /* CLI logic */
	
	static void usage(Options options) {
	    
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("FileExporter [flags] filename.hX", options );
		System.exit(1);
		}
	
    public static void main(String[] args) throws Exception {
    	
    	Options options = new Options();
    	options.addOption(new Option("h", "help", false, "print this message"));
    	options.addOption(new Option(null, "skip-own-planets", false, "Don't export your own planets"));
    	options.addOption(new Option("o", "output", true, "Output file name (default: infile.json)"));
    	
        CommandLine line = null;
        try {
            line = new DefaultParser().parse( options, args );
        } catch( ParseException exp ) {
            System.err.println("Cannot parse options: "+exp.getMessage());
        	usage(options);
        }
        String[] arguments = line.getArgs();
        if (line.hasOption("h") || arguments.length != 1) usage(options);
        String filename = arguments[0];
        
        PrintWriter out = line.hasOption("o")?new PrintWriter(line.getOptionValue("o")):new PrintWriter(System.out);
        
        FileExporter exporter = new FileExporter(line);
        Object blocks = exporter.parseFiles(filename);
        
        writeBlocks(blocks, out);
    }  

    static void writeBlocks(Object blocks, PrintWriter out) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        out.println(gson.toJson(blocks));
        out.flush();
    }
    
    /* Parse file logic */
    
    CommandLine options;
    int player;
    
    public FileExporter(CommandLine options) {
    	this.options = options;
    }
    
    public Map<String, List<? extends Block>> parseFiles(String filename) throws Exception {
        List<Block> blocks;
        blocks = new Decryptor().readFile(filename);
        check(blocks);
        
        List<PartialPlanetBlock> planets = new Vector<PartialPlanetBlock>();
        
        FileHeaderBlock headerBlock = (FileHeaderBlock) blocks.get(0);
        this.player = headerBlock.playerNumber;
        System.err.println("File "+filename+", player:" +(this.player+1)+", turn: "+ headerBlock.turn);
        
        for (Block block : blocks) {
        	if (block instanceof PartialPlanetBlock) {
        		PartialPlanetBlock p = (PartialPlanetBlock) block;
        		if (this.options.hasOption("skip-own-planets") && p.owner == this.player ) continue;
        		planets.add(p);
        	}
        }
        List<? extends Block> x = new Vector<Block>();
        
        Map<String, List<? extends Block>> out = new HashMap<String, List<? extends Block>>();
        out.put("planets", planets);
        
        return out;
       
    }
    

	private static boolean check(List<Block> blocks) throws Exception {
        if (blocks == null || blocks.size() == 0) {
            throw new Exception("Cannot parse block list");
        }
        if (blocks.get(0).typeId != BlockType.FILE_HEADER) {
            throw new Exception("Does not start with header block");
        }
        FileHeaderBlock headerBlock = (FileHeaderBlock)blocks.get(0);
        int fileType = headerBlock.fileType;
        if (fileType != 4 && fileType != 3) {
            throw new Exception("Unrecognized file type");
        }
        if (fileType == 3) return false;
        if (blocks.size() < 4) {
            throw new Exception("File does not have expected structure");
        }
        if (blocks.get(1).typeId != BlockType.COUNTERS) {
            throw new Exception("File does not have expected structure");
        }
        CountersBlock counters = (CountersBlock)blocks.get(1);
        int numPlanets = counters.planetCount;
        if (blocks.size() < 3 + numPlanets) {
            throw new Exception("File does not have expected structure");
        }
        for (int i = 0; i < numPlanets; i++) {
            if (blocks.get(2 + i).typeId != BlockType.PARTIAL_PLANET) {
                throw new Exception("File does not have expected structure");
            }
        }
        for (int i = 2 + numPlanets; i < blocks.size(); i++) {
            if (blocks.get(i).typeId == BlockType.PARTIAL_PLANET) {
                throw new Exception("File does not have expected structure");
            }
        }
        return false;
    }
    
}
