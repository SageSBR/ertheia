package databuilder.xml.holder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.reward.RewardData;
import l2s.gameserver.templates.item.ArmorTemplate;
import l2s.gameserver.templates.item.EtcItemTemplate;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import databuilder.utils.XmlPretty;

/**
 * @author: VISTALL
 * @date:  16:15/14.12.2010
 */
public final class L2onDropHolder extends AbstractHolder
{
	private static final L2onDropHolder _instance = new L2onDropHolder();

	private HashMap<String, Element> _rootHolder = new HashMap<String, Element>();
	private HashMap<Integer, Element> _l2sHolder = new HashMap<Integer, Element>();
	private TreeMap<Integer, L2DropInfo> _dropHolder = new TreeMap<Integer, L2DropInfo>();
	
	public class L2DropInfo{
		
		int _id;
		
		Element _element;
		
		HashMap<String,ArrayList<RewardData>> _droplist = new HashMap<String,ArrayList<RewardData>>(); 
		ArrayList<RewardData> _spoillist = new ArrayList<RewardData>();
		RewardData _adena = null;
		
		public L2DropInfo(int npc_id){
			_id = npc_id;
			_dropHolder.put(_id, this);
		}
		
		public void addDrop(RewardData item){
			if(item.getItem().isAdena())
			{
				_adena = item;
				return;
			}
			
			String category = "_none_";
			
			ItemTemplate itemTemplate = item.getItem();
			
			if(item.getChance() > 999999)
				category = Integer.toString(item.getItemId());
			else if(itemTemplate instanceof WeaponTemplate)
			{
				if(itemTemplate.isMagicWeapon()){
					category = "isWeapon-magic-"+item.getItem().getGrade();
				}else{
					category = "isWeapon-"+item.getItem().getGrade();
				}
				
			}
			else if(itemTemplate instanceof ArmorTemplate)
			{
				if(itemTemplate.isAccessory())
					category = "isArmor-"+item.getItem().getGrade();
				else if(itemTemplate.isAccessory())
					category = "isAccessory"+item.getItem().getGrade();
			}
			else if(itemTemplate instanceof EtcItemTemplate){
				category = itemTemplate.getItemType().toString();
			}
				
			
			
			ArrayList<RewardData> categoryList = _droplist.get(category);
			
			if(categoryList == null)
			{
				categoryList = new ArrayList<RewardData>();
				_droplist.put(category, categoryList);
			}

			categoryList.add(item);
		}
		
		public void addSpoil(RewardData item){
			_spoillist.add(item);
		}
		
		public Element getElement()
		{
			
			if(_element == null){
				_element = _l2sHolder.get(_id);
				if(_element != null)
					getRoot(_id).add(_element.detach());
			}
			
			if(_element == null)
			{
				System.out.println("Missing:"+_id);
				
				_element = getRoot(_id).addElement("npc");
				getRoot(_id).addComment("Generated by blood");
				_element.addAttribute("id", Integer.toString(_id));
				
				
			}
			
			for(Element childElement: _element.elements())
			{
				if(childElement.getName().equalsIgnoreCase("rewardlist"))
				{
					childElement.detach();
				}
			}
			
			if(_droplist.size() > 0 || _adena != null){
				Element dropElement = _element.addElement("rewardlist");
				dropElement.addAttribute("type", "RATED_GROUPED");
				
				if(_adena != null)
				{
					dropElement.addElement("group")
					.addAttribute("chance", _adena.getChance() > 999999 ? "100": "70")
					.addElement("reward")
					.addAttribute("item_id", Integer.toString(_adena.getItemId()))
					.addAttribute("min", Long.toString(_adena.getMinDrop()))
					.addAttribute("max", Long.toString(_adena.getMaxDrop()))
					.addAttribute("chance", "100");
				}
				
				for(ArrayList<RewardData> categoryList: _droplist.values())
				{
					
					
					
					ArrayList<ArrayList<RewardData>> subLists = new ArrayList<ArrayList<RewardData>>();
					ArrayList<RewardData> subList = new ArrayList<RewardData>();
					subLists.add(subList);
					int groupChance = 0;
					
					for(RewardData reward: categoryList)
					{
						if(groupChance + reward.getChance() > 1000000){
							subList = new ArrayList<RewardData>();
							subLists.add(subList);
							groupChance = 0;
						}
						groupChance += reward.getChance();
						subList.add(reward);
					}
					
					for(ArrayList<RewardData> _subList: subLists)
					{
						groupChance = 0;
						
						for(RewardData reward: _subList)
						{
							groupChance += reward.getChance();
						}
						
						Element groupElement = dropElement.addElement("group");
						groupElement.addAttribute("chance", String.format("%.2f", groupChance/10000.));
						
						double usedChance = 0;
						
						for(int i = 0; i < _subList.size(); i++)
						{
							RewardData reward = _subList.get(i);
							double itemChance = Math.round(reward.getChance()*100/groupChance);
							if(i == categoryList.size() - 1){
								itemChance = 100 - usedChance;
							}else{
								usedChance += itemChance;
							}
							groupElement.addElement("reward")
							.addAttribute("item_id", Integer.toString(reward.getItemId()))
							.addAttribute("min", Long.toString(reward.getMinDrop()))
							.addAttribute("max", Long.toString(reward.getMaxDrop()))
							.addAttribute("chance", String.format("%.2f",itemChance));
							groupElement.addComment(reward.getItem().getName());
						}
					}
					
					
				}
				
			}
			
			if(_spoillist.size() > 0)
			{
				Element spoilElement = _element.addElement("rewardlist");
				spoilElement.addAttribute("type", "SWEEP");
				
				for(RewardData reward: _spoillist){
					spoilElement.addElement("reward")
					.addAttribute("item_id", Integer.toString(reward.getItemId()))
					.addAttribute("min", Long.toString(reward.getMinDrop()))
					.addAttribute("max", Long.toString(reward.getMaxDrop()))
					.addAttribute("chance", Double.toString(reward.getChance()/10000));
					spoilElement.addComment(reward.getItem().getName());
				}
			}
			
			
			return _element;
		}
		
	}

	public static L2onDropHolder getInstance()
	{
		return _instance;
	}

	L2onDropHolder()
	{

	}
	
	public void addL2sElement(int npc_id, Element e){
		_l2sHolder.put(npc_id, e);
		getDropInfo(npc_id);
	}
	
	public Element getRoot(int id){
		int min_id = id - id%100;
		int max_id = min_id + 99;
		return getRoot(String.format("%05d-%05d", min_id, max_id));
	}
	
	public Element getRoot(String file){
		
		Element resultElement = _rootHolder.get(file);
		
		if(resultElement == null)
		{
			Document document = DocumentHelper.createDocument();
			resultElement = document.addElement("list");
			_rootHolder.put(file, resultElement);
		}
		
		return resultElement;
	}

	public L2DropInfo getDropInfo(int id)
	{
		return _dropHolder.get(id) != null ? _dropHolder.get(id) : new L2DropInfo(id);
	}
	
	public void store(){
		for(Map.Entry<String, Element> entry: _rootHolder.entrySet())
			XmlPretty.writeToFile(entry.getKey(), entry.getValue().asXML(), "npc.dtd", "data/blood_droplist/");
	}
	
	public void build()
	{
		int count = 0;
		for(L2DropInfo info: _dropHolder.values())
		{
			info.getElement();
			count++;
			if(count%100 == 0){
				System.out.println("processed item: "+count);
			}
		}
	}
	
	
	@Override
	protected void process()
	{
		build();
		store();
	}
	

	@Override
	public int size()
	{
		return _dropHolder.size();
	}

	@Override
	public void clear()
	{
		_dropHolder.clear();
	}
}