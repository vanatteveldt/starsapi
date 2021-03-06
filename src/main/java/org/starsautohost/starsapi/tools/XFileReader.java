package org.starsautohost.starsapi.tools;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import org.starsautohost.starsapi.block.Block;
import org.starsautohost.starsapi.tools.GalaxyViewer.MapFileData;

/**
 * @author Runar
 *
 */
public class XFileReader {
	
	public static void main(String[] args) throws Exception{
		try{
			if (args.length < 1){
				System.out.println("Usage: <xfile>");
				System.out.println("m-file and map-file must exist in same directory as x-file!");
				System.exit(1);
			}
			File xFile = new File(args[0]);
			if (xFile.exists() == false) xFile = new File(new File(".").getParentFile(),args[0]);
			if (xFile.exists() == false) throw new Exception("Could not find x-file "+args[0]);

			String[] el = xFile.getName().split("\\.");
			File mFile = new File(xFile.getParentFile(),el[0]+"."+el[1].replace("x","m").replace("X","M"));
			File mapFile = new File(xFile.getParentFile(),el[0]+"."+el[1].substring(0,1).replace("x","map").replace("X","MAP"));
			if (mFile.exists() == false) throw new Exception("Could not find m-file "+mFile.getAbsolutePath());
			if (mapFile.exists() == false){
				if (xFile.getParentFile() != null){ //In case of old subdirectories, check parent directory for map file.
					File m = new File(xFile.getParentFile().getParentFile(),el[0]+"."+el[1].substring(0,1).replace("x","map").replace("X","MAP"));
					if (m.exists()) mapFile = m;
				}
				if (mapFile.exists() == false) throw new Exception("Could not find map-file "+mapFile.getAbsolutePath());
			}
			MapFileData map = GalaxyViewer.parseMapFileData(mapFile);
			PlayerState s = new PlayerState(mFile,xFile,map);
			//printHistogram(s.xBlocks);
			String error = s.sanitize();
			if (error != null){
				System.err.println(error);
				System.exit(1);
			}
			System.out.println("X-file OK!");
			System.exit(0);
		}
		catch(Exception ex){
			System.err.println(ex.toString());
			ex.printStackTrace(System.out);
			System.exit(1);
		}
	}
	
	public static void printHistogram(List<Block> list) {
		HashMap<Class<?>,Integer> hm = new HashMap<Class<?>, Integer>();
		for (Block b : list){
			Integer i = hm.get(b.getClass());
			if (i == null) i = 0;
			hm.put(b.getClass(),i+1);
			//System.out.println(b.getClass().getName());
		}
		for (Class<?> c : hm.keySet()){
			System.out.println(c.getName()+"\t"+hm.get(c));
		}
	}
}
