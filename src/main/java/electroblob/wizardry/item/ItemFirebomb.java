package electroblob.wizardry.item;

import electroblob.wizardry.entity.projectile.EntityFirebomb;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemFirebomb extends Item {

	public ItemFirebomb(){
		setMaxStackSize(16);
		setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){

		ItemStack stack = player.getHeldItem(hand);

		if(!player.capabilities.isCreativeMode){
			stack.shrink(1);
		}

		player.playSound(SoundEvents.ENTITY_SNOWBALL_THROW, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

		if(!world.isRemote){
			EntityFirebomb firebomb = new EntityFirebomb(world, player);
			// This is the standard set of parameters for this method, used by snowballs and ender pearls.
			firebomb.shoot(player, player.rotationPitch, player.rotationYaw, 0.0f, 1.5f, 1.0f);
			world.spawnEntity(firebomb);
		}

		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}
	
}