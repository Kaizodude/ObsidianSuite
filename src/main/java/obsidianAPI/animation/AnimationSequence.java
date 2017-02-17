package obsidianAPI.animation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.Constants;
import obsidianAPI.render.ModelObj;
import obsidianAPI.render.part.Part;

import java.util.*;

/**
 * An actual animation. Comprised of animation parts - sections for each part of the model.
 *
 */
public class AnimationSequence
{

	private String animationName;
	private final Map<String, TreeMap<Integer,AnimationPart>> partsByPartName = Maps.newHashMap();
	private final Map<Integer, Set<String>> actionPoints = Maps.newHashMap();
	private int fps;
	private String entityName;

	public AnimationSequence(String entityName, String animationName)
	{
		this.entityName = entityName;
		this.animationName = animationName;
		this.fps = 25;
	}

	public AnimationSequence(NBTTagCompound compound)
	{
		this.loadData(compound);
	}

	public String getEntityName()
	{
		return entityName;
	}

	public String getName()
	{
		return animationName;
	}

	public void setAnimations(List<AnimationPart> animations)
	{
		partsByPartName.clear();
		for (AnimationPart part : animations)
			addAnimationToMap(part);
	}

	private void addAnimationToMap(AnimationPart part)
	{
		TreeMap<Integer, AnimationPart> parts = partsByPartName.get(part.getPartName());
		if (parts == null)
		{
			parts = new TreeMap<>();
			partsByPartName.put(part.getPartName(), parts);
		}

		parts.put(part.getStartTime(), part);
	}

	public List<AnimationPart> getAnimationList()
	{
		List<AnimationPart> animationList = new ArrayList<AnimationPart>();
		for(String partName : partsByPartName.keySet())
			animationList.addAll(getAnimations(partName));
		return animationList;
	}

	public Collection<AnimationPart> getAnimations(String partName)
	{
		TreeMap<Integer, AnimationPart> parts = partsByPartName.get(partName);
		if (parts == null)
		{
			return Collections.emptyList();
		}
		return parts.values();
	}

	public void addAnimation(AnimationPart part)
	{
		addAnimationToMap(part);
	}

	public void clearAnimations()
	{
		partsByPartName.clear();
	}

	public Collection<String> getActionPoints(int time)
	{
		Set<String> names = actionPoints.get(time);
		if (names == null)
		{
			names = Collections.emptySet();
		}
		return ImmutableList.copyOf(names);
	}

	public void addActionPoint(int time, String name)
	{
		Set<String> names = actionPoints.get(time);
		if (names == null)
		{
			names = Sets.newHashSet();
			actionPoints.put(time,names);
		}

		names.add(name);
	}

	public void removeActionPoint(int time, String name)
	{
		Set<String> names = actionPoints.get(time);
		if (names != null)
		{
			names.remove(name);

			if (names.size() == 0)
			{
				actionPoints.remove(time);
			}
		}
	}

	public int getFPS()
	{
		return fps;
	}

	public void setFPS(int fps)
	{
		this.fps = fps;
	}

	/**
	 * Return true if the given partname has two or more animation parts associated with it.
	 */
	public boolean multiPartSequence(String partName)
	{
		return getAnimations(partName).size() >= 2;
	}

	public void animateAll(float time, ModelObj entityModel)
	{
		animateAll(time, entityModel, "");
	}

	/**
	 * Sets all the parts of a model to their rotation at a given time.
	 * The part with name = exceptionPartName will not be rotated.
	 */
	public void animateAll(float time, ModelObj entityModel, String exceptionPartName)
	{
		for(Part part : entityModel.parts)
		{
			if(!part.getName().equals(exceptionPartName))
			{
				TreeMap<Integer, AnimationPart> animations = partsByPartName.get(part.getName());
				if(animations != null && animations.size() > 0)
				{
					AnimationPart anim = findPartForTime(animations, MathHelper.floor_float(time));
					if (anim == null)
						anim = animations.lastEntry().getValue();
					anim.animatePart(part, time - anim.getStartTime());
				}
			}
		}
	}

	public Map<String, float[]> getPartValuesAtTime(ModelObj entityModel, float time)
	{
		Map<String, float[]> partValues = new HashMap<String, float[]>();
		for(Part part : entityModel.parts)
		{
			TreeMap<Integer, AnimationPart> animations = partsByPartName.get(part.getName());
			if(animations != null && animations.size() > 0)
			{
				AnimationPart anim = findPartForTime(animations, MathHelper.floor_float(time));
				if (anim == null)
					anim = animations.lastEntry().getValue();
				partValues.put(part.getName(), anim.getPartRotationAtTime(time - anim.getStartTime()));
			}
			else
				partValues.put(part.getName(), part.getOriginalValues());
		}
		return partValues;
	}

	private AnimationPart findPartForTime(TreeMap<Integer,AnimationPart> parts, int time)
	{
		Map.Entry<Integer, AnimationPart> entry = parts.floorEntry(time);
		if (entry != null)
		{
			return entry.getValue();
		}

		return null;
	}

	//TODO AnimatinSequence - would it be better to store this rather than calculate it every time?
	public int getTotalTime()
	{
		int max = 0;
		for(AnimationPart animation : getAnimationList())
		{
			if(animation.getEndTime() > max)
			{
				max = animation.getEndTime();
			}
		}
		return max;
	}

	public AnimationSequence copy()
	{
		AnimationSequence sequence = new AnimationSequence(entityName,animationName);
		sequence.loadData(getSaveData());
		return sequence;
	}

	public NBTTagCompound getSaveData()
	{
		NBTTagCompound sequenceData = new NBTTagCompound();
		NBTTagList animationList = new NBTTagList();
		for(AnimationPart animation : getAnimationList())
			animationList.appendTag(animation.getSaveData());
		sequenceData.setTag("Animations", animationList);
		sequenceData.setString("EntityName", entityName);
		sequenceData.setString("Name", animationName);
		sequenceData.setInteger("FPS", fps);
		sequenceData.setTag("Actions", getActionsSaveData());
		return sequenceData;
	}

	private NBTTagList getActionsSaveData()
	{
		NBTTagList actionList = new NBTTagList();
		for (Map.Entry<Integer, Set<String>> entry : actionPoints.entrySet())
		{
			int time = entry.getKey();
			NBTTagList nameList = new NBTTagList();
			for (String name : entry.getValue())
			{
				nameList.appendTag(new NBTTagString(name));
			}

			NBTTagCompound timeCompound = new NBTTagCompound();
			timeCompound.setInteger("Time", time);
			timeCompound.setTag("Names", nameList);

			actionList.appendTag(timeCompound);
		}
		return actionList;
	}

	public void loadData(NBTTagCompound compound)
	{
		entityName = compound.getString("EntityName");
		NBTTagList segmentList = compound.getTagList("Animations", 10);
		for(int i = 0; i < segmentList.tagCount(); i++)
		{
			AnimationPart animation = new AnimationPart(segmentList.getCompoundTagAt(i));
			addAnimation(animation);
		}
		animationName = compound.getString("Name");
		fps = compound.hasKey("FPS") ? compound.getInteger("FPS") : 25;

		loadActions(compound.getTagList("Actions", Constants.NBT.TAG_COMPOUND));
	}

	private void loadActions(NBTTagList actionsList)
	{
		for (int i = 0; i < actionsList.tagCount(); i++)
		{
			NBTTagCompound timeCompound = actionsList.getCompoundTagAt(i);
			int time = timeCompound.getInteger("Time");
			NBTTagList nameList = timeCompound.getTagList("Names", Constants.NBT.TAG_STRING);
			for (int j = 0; j < nameList.tagCount(); j++)
			{
				String name = nameList.getStringTagAt(j);

				addActionPoint(time, name);
			}
		}
	}
}
