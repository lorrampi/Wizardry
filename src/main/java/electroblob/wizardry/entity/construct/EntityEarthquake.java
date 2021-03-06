package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityEarthquake extends EntityMagicConstruct {

	public EntityEarthquake(World world){
		super(world);
		this.height = 1.0f;
		this.width = 1.0f;
	}

	public EntityEarthquake(World world, double x, double y, double z, EntityLivingBase caster, int lifetime,
			float damageMultiplier){
		super(world, x, y, z, caster, lifetime, damageMultiplier);
		this.height = 1.0f;
		this.width = 1.0f;
	}

	public void onUpdate(){

		super.onUpdate();

		if(!world.isRemote){

			double speed = 0.4;

			// The further the earthquake is going to spread, the finer the angle increments.
			for(double angle = 0; angle < 2 * Math.PI; angle += Math.PI / (lifetime * 1.5)){

				// Calculates coordinates for the block to be moved. The radius increases with time. The +1.5 is to
				// leave
				// blocks in the centre untouched.
				int x = this.posX < 0 ? (int)(this.posX + ((this.ticksExisted * speed) + 1.5) * Math.sin(angle) - 1)
						: (int)(this.posX + ((this.ticksExisted * speed) + 1.5) * Math.sin(angle));
				int y = (int)(this.posY - 0.5);
				int z = this.posZ < 0 ? (int)(this.posZ + ((this.ticksExisted * speed) + 1.5) * Math.cos(angle) - 1)
						: (int)(this.posZ + ((this.ticksExisted * speed) + 1.5) * Math.cos(angle));

				BlockPos pos = new BlockPos(x, y, z);

				if(!WizardryUtilities.isBlockUnbreakable(world, pos) && !world.isAirBlock(pos)
						&& world.isBlockNormalCube(pos, false)
						// Checks that the block above is not solid, since this causes the falling sand to vanish.
						&& !world.isBlockNormalCube(pos.up(), false)){

					// Falling blocks do the setting block to air themselves.
					EntityFallingBlock fallingblock = new EntityFallingBlock(world, x + 0.5, y + 0.5, z + 0.5,
							world.getBlockState(new BlockPos(x, y, z)));
					fallingblock.motionY = 0.3;
					world.spawnEntity(fallingblock);
				}
			}

			List<EntityLivingBase> targets = WizardryUtilities
					.getEntitiesWithinRadius((this.ticksExisted * speed) + 1.5, this.posX, this.posY, this.posZ, world);

			// In this particular instance, the caster is completely unaffected because they will always be in the
			// centre.
			targets.remove(this.getCaster());

			for(EntityLivingBase target : targets){

				// Searches in a 1 wide ring.
				if(this.getDistance(target) > (this.ticksExisted * speed) + 0.5 && target.posY < this.posY + 1
						&& target.posY > this.posY - 1){

					// Knockback must be removed in this instance, or the target will fall into the floor.
					double motionX = target.motionX;
					double motionZ = target.motionZ;

					if(this.isValidTarget(target)){
						target.attackEntityFrom(
								MagicDamage.causeIndirectMagicDamage(this, this.getCaster(), DamageType.BLAST),
								10 * this.damageMultiplier);
						target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 400, 1));
					}

					// All targets are thrown, even those immune to the damage, so they don't fall into the ground.
					target.motionX = motionX;
					target.motionY = 0.8; // Throws target into the air.
					target.motionZ = motionZ;

					// Player motion is handled on that player's client so needs packets
					if(target instanceof EntityPlayerMP){
						((EntityPlayerMP)target).connection.sendPacket(new SPacketEntityVelocity(target));
					}
				}
			}
			// TODO: Uncomment once 2.1.0 is released
			// }else{
			//
			// // Constant 15 blocks for now
			// List<EntityPlayer> targets = WizardryUtilities.getEntitiesWithinRadius(15, this.posX, this.posY,
			// this.posZ, world, EntityPlayer.class);
			//
			// float magnitude = 6f * ((float)(this.lifetime - this.ticksExisted))/(float)this.lifetime;
			//
			// // Makes the screen shake
			// for(EntityLivingBase target : targets){
			// target.setAngles(0, this.ticksExisted % 4 < 2 ? magnitude : -magnitude);
			// }
		}
	}

}
