package databuilder;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.parser.BaseStatsBonusParser;
import l2s.gameserver.data.xml.parser.LevelBonusParser;
import l2s.gameserver.data.xml.parser.NpcParser;
import l2s.gameserver.tables.SkillTable;
import databuilder.xml.builder.ItemBuilder;
import databuilder.xml.parser.ItemParser;
import databuilder.xml.parser.NpcGaiParser;

public class MainBuilder
{
	
	public static MainBuilder _instance;
	public static Connection _conn = null;
	public static final String _datapack_path = "/Users/mylove1412/Workspace/ertheia/dist/gameserver/";
	public static String _jdbc_connection = "jdbc:mysql://localhost/l2_raw?user=root&password=";
	
	private MainBuilder()
	{
		//BloodFakePlayers.getInstance();
	}
	
	public static Connection connection(){
		
		if(_conn == null)
		{
			try {
				_conn = DriverManager.getConnection(_jdbc_connection);

			    // Do something with the Connection

			} catch (SQLException ex) {
			    // handle any errors
			    System.out.println("SQLException: " + ex.getMessage());
			    System.out.println("SQLState: " + ex.getSQLState());
			    System.out.println("VendorError: " + ex.getErrorCode());
			}
		}
		return _conn;
	}
	
	public static void buildNpc(){
		
		SkillTable.getInstance().load();
		BaseStatsBonusParser.getInstance().load();
		LevelBonusParser.getInstance().load();
		NpcParser.getInstance().load();
		NpcGaiParser.getInstance().load();
	}
	
	
	
	public static void main(String[] args)
	{
//		System.currentTimeMillis();
		System.out.println("Start...");
		try {
			Config.DATAPACK_ROOT = new File(_datapack_path).getCanonicalFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		SkillTable.getInstance().load();
//		OptionDataParser.getInstance().load();
//		VariationDataParser.getInstance().load();
		ItemParser.getInstance().load();
		
		
		
		ItemBuilder.getInstance().build();
		ItemBuilder.getInstance().store();
		
	}
	
}