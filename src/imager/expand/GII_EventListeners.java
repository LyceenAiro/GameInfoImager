package imager.expand;

import arc.Core;
import arc.Events;
import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Cons2;
import arc.func.Cons3;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.struct.Seq;
import arc.util.*;
import arc.util.pooling.Pools;
import imager.GII_Plugin;
import imager.content.NHPColor;
import imager.content.NHPShaders;
import imager.expand.ui.GII_Vars;
import imager.expand.ui.PointerDraw;
import imager.expand.ui.UnitInfo;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.blocks.defense.turrets.Turret;


public class GII_EventListeners{
	public static final Rect viewport = new Rect();
	
	public static final Cons3<Teamc, Float, Float> drawer = (entity, range, size) -> {
		if(entity.team() == Team.derelict)return;
		boolean ally = entity.team() == Vars.player.team();
		if(ally && !GII_Plugin.drawAlly)return;
		Color c = ally ? NHPColor.ally : NHPColor.hostile;
		
		Fill.light(entity.x(), entity.y(), Lines.circleVertices(range), range, c, c);
	};
	
	public static final Cons3<Teamc, Float, Float> drawer2 = (entity, range, size) -> {
		if(entity.team() == Team.derelict)return;
		boolean ally = entity.team() == Vars.player.team();
		if(ally && !GII_Plugin.drawAlly)return;
		Color c = ally ? NHPColor.ally2 : NHPColor.hostile2;
		
		Draw.color(c);
		
		Lines.lineAngle(entity.x(), entity.y(), 45, range);
		Lines.circle(entity.x(), entity.y(), range);
		Fill.circle(entity.x(), entity.y(), Lines.getStroke() * 1.3f);
	};
	
	public static class DrawPair<T>{
		public Boolf<T> bool;
		public Cons<T> drawer;
		
		public DrawPair(Boolf<T> bool, Cons<T> drawer){
			this.bool = bool;
			this.drawer = drawer;
		}
	}
	
	public static final Seq<DrawPair<Unit>> signDrawer = new Seq<>(5);
	
	public static final Vec2[]
		T1 = {new Vec2().trns(90, 1.65f), new Vec2().trns(90 + 120, 1.65f), new Vec2().trns(90 - 120, 1.65f)},
		T2 = {new Vec2().trns(0, 1.65f), new Vec2().trns(90, 1.65f), new Vec2().trns(180, 1.65f), new Vec2().trns(270, 1.65f)},
		T3 = {
			new Vec2().trns(0  , 0.6f),
			new Vec2().trns(60 , 0.6f),
			new Vec2().trns(120, 0.6f),
			new Vec2().trns(180, 0.6f),
			new Vec2().trns(240, 0.6f),
			new Vec2().trns(300, 0.6f)
		},
		T4 = {
			new Vec2(1.4f, 0.45f),
			new Vec2(-0.6f, 0.45f),
			new Vec2(-1.4f, -0.45f),
			new Vec2(0.6f, -0.45f),
		},
		T5_N = {
			new Vec2(1f, 0),
			new Vec2(0.5f, 0.5f),
			new Vec2(-1f, 0.5f),
			new Vec2(-0.5f, 0),
			new Vec2(-1f, -0.5f),
			new Vec2(0.5f, -0.5f)
		};
		
	public static final Cons2<Vec2[], Unit> drawFunc = ((vs, unit) -> {
		if(unit.team() == Team.derelict)return;
		
		Draw.color(Pal.gray);
		Lines.stroke(2.8f);
		poly(vs, unit.x, unit.y, unit.hitSize() + 3);
		
		Tmp.c1.set(unit.type.cellColor(unit)).lerp(Color.white, Mathf.absin(5f, 0.4f));
		
		
		Draw.color(Tmp.c1);
		Lines.stroke(1);
		poly(vs, unit.x, unit.y, unit.hitSize() + 3);
		
		Tmp.c1.set(unit.team.color).lerp(Color.white, Mathf.absin(5f, 0.4f));
		
		float z = Draw.z();
		Draw.z(z + 0.1f);
		if(unit.type.health > 1000){
			Font font = Fonts.outline;
			GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
			boolean ints = font.usesIntegerPositions();
			font.setUseIntegerPositions(false);
			font.getData().setScale(0.35f / Scl.scl(1.0f));
			layout.setText(font, unit.type.localizedName);
			font.setColor(Tmp.c1);
			
			font.draw(unit.type.localizedName, unit.x, unit.y + font.getData().lineHeight / 2.6f, 1);
			
			font.setUseIntegerPositions(ints);
			font.setColor(Color.white);
			font.getData().setScale(1.0F);
			Draw.reset();
			Pools.free(layout);
		}
		Draw.z(z);
	});
	
	public static void poly(Vec2[] vertices, float offsetx, float offsety, float scl){
		for(int i = 0; i < vertices.length; i++){
			Vec2 current = vertices[i];
			Vec2 next = i == vertices.length - 1 ? vertices[0] : vertices[i + 1];
			Lines.line(current.x * scl + offsetx, current.y * scl + offsety, next.x * scl + offsetx, next.y * scl + offsety, false);
			Fill.circle(current.x * scl + offsetx, current.y * scl + offsety, Lines.getStroke() / 2);
		}
	}
	
	public static Seq<Building> builds = new Seq<>();
	public static Seq<Unit> units = new Seq<>();
	public static final Seq<Building> buildsUTD = new Seq<>();
	public static final Seq<Unit> unitsUTD = new Seq<>();
	
	public static int minBuildSize;
	public static float minUnitSize;
	
	public static int lastID;
	
	public static Interval timer = new Interval(2);
	
	public static TaskQueue taskQueue = new TaskQueue();
	public static Thread updateThread;
	
	public static void stop(){
		if(updateThread != null){
			updateThread.interrupt();
			updateThread = null;
		}
		
		taskQueue.clear();
	}
	
	@SuppressWarnings("BusyWait")
	public static void start(){
		stop();
		updateThread = Threads.daemon("Scanner", () -> {
			while(true){
				try{
					if(Vars.state.isPlaying()){
						taskQueue.run();
					}
					
					try{
						Thread.sleep(3);
					}catch(InterruptedException e){
						//stop looping when interrupted externally
						break;
					}
				}catch(Exception e){
					Log.err(e);
				}
			}
		});
	}
	
	public static final Boolf<Building> addBuilding = b -> b instanceof Turret.TurretBuild && ((Turret.TurretBuild)b).hasAmmo() && b.isValid() && b.block.size >= minBuildSize;
	public static final Boolf<Unit> addUnit = u -> u.isValid() && u.hitSize() >= minUnitSize * Vars.tilesize;
	
	public static void load(){
		signDrawer.add(new DrawPair<>(unit -> unit.type.health > 12000, unit -> drawFunc.get(T5_N, unit)));
		signDrawer.add(new DrawPair<>(unit -> unit.type.health > 6000, unit -> drawFunc.get(T4, unit)));
		signDrawer.add(new DrawPair<>(unit -> unit.type.health > 600, unit -> drawFunc.get(T3, unit)));
		signDrawer.add(new DrawPair<>(unit -> unit.type.health > 400, unit -> drawFunc.get(T2, unit)));
		signDrawer.add(new DrawPair<>(unit -> true, unit -> drawFunc.get(T1, unit)));
		
		Events.on(EventType.ClientLoadEvent.class, e -> {
		
		});
		
		if(!Vars.headless)Events.on(EventType.WorldLoadEvent.class, e -> {
			minBuildSize = Core.settings.getInt(GII_Plugin.BUILDING_SIZE_FILTER, 1);
			minUnitSize = Core.settings.getInt(GII_Plugin.UNIT_SIZE_FILTER, 0);
			
			builds.clear();
			units.clear();
			
			Groups.build.copy(builds).retainAll(addBuilding);
			Groups.unit.copy(units).retainAll(addUnit);
			
			start();
			
			Core.app.post(UnitInfo::addBars);
		});
		
		Events.run(EventType.Trigger.update, () -> {
			if(Vars.state.isMenu())stop();
			
			if(!Vars.state.isGame())return;
			Vec2 wp = Core.camera.unproject(Core.input.mouse());
			GII_Vars.currentBuilding = Vars.world.build(World.toTile(wp.x), World.toTile(wp.y));
			GII_Vars.currentUnit = null;
			Groups.unit.intersect(wp.x - 2, wp.y - 2, 4, 4, u -> {
				GII_Vars.currentUnit = u;
			});
			PointerDraw.update();
			
			GII_Plugin.showHealthBar = Core.settings.getBool(GII_Plugin.SHOW_UNIT_HEALTH_BAR, true);
			
			// if(timer.get(1, 30f)){
			// 	// taskQueue.post(() -> {
			// 	// 	synchronized(unitsUTD){
			// 	// 		unitsUTD.clear();
			// 	// 		Groups.unit.copy(unitsUTD).filter(addUnit);
			// 	// 		units = new Seq<>(unitsUTD);
			// 	// 	}
			// 	// });

			// 	UnitInfo.update();
			// }
			
			if(timer.get(1, 30f)){
				taskQueue.post(() -> {
					synchronized(buildsUTD){
						buildsUTD.clear();
						Groups.build.copy(buildsUTD).retainAll(addBuilding);
						builds = new Seq<>(buildsUTD);
					}
				});
			}
		});
		
		Events.on(EventType.TilePreChangeEvent.class, e -> {
			if(e.tile.build != null && lastID != e.tile.build.id && addBuilding.get(e.tile.build)){
				lastID = e.tile.build.id;
				builds.remove(e.tile.build);
			}
		});
		
		Events.on(EventType.TileChangeEvent.class, e -> {
			if(e.tile.build != null && addBuilding.get(e.tile.build)){
				builds.add(e.tile.build);
			}
		});
		
		Events.on(EventType.UnitDestroyEvent.class, e -> {
			if(e.unit != null && addUnit.get(e.unit)){
				units.remove(e.unit);
				UnitInfo.update();
			}
		});
		
		Events.on(EventType.UnitCreateEvent.class, e -> {
			if(e.unit != null && addUnit.get(e.unit)){
				units.add(e.unit);
				UnitInfo.update();
			}
		});

		// Events.on(EventType.UnitUnloadEvent.class, e -> {
		// 	if(e.unit != null && addUnit.get(e.unit)){
		// 		UnitInfo.update();
		// 	}
		// });

		Events.on(EventType.UnitSpawnEvent.class, e -> {
			if(e.unit != null && addUnit.get(e.unit)){
				UnitInfo.update();
			}
		});
		
		Events.run(EventType.Trigger.draw, () -> {
			float z = Draw.z();
			
			minBuildSize = Core.settings.getInt(GII_Plugin.BUILDING_SIZE_FILTER, 1);
			minUnitSize = Core.settings.getInt(GII_Plugin.UNIT_SIZE_FILTER, 0);
			
			Core.camera.bounds(viewport);
			
			Seq<Unit> us = units.copy().retainAll(draw -> viewport.overlaps(draw.x() - draw.range(), draw.y() - draw.range(), draw.range() * 2, draw.range() * 2));
			Seq<Building> bs = builds.copy().retainAll(entity -> {
				BaseTurret.BaseTurretBuild draw = (BaseTurret.BaseTurretBuild)entity;
				return viewport.overlaps(draw.x() - draw.range(), draw.y() - draw.range(), draw.range() * 2, draw.range() * 2);
			});
			
			PointerDraw.draw();
			
			if(Core.settings.getBool(GII_Plugin.SETTING_KEY, true)){
				Draw.draw(Layer.space + 10.55f, () -> {
					Vars.renderer.effectBuffer.begin(Color.clear);
					
					us.each(entity -> drawer.get(entity, entity.range(), entity.hitSize()));
					bs.each(entity -> drawer.get(entity, ((BaseTurret.BaseTurretBuild)entity).range(), entity.hitSize()));
					
					Vars.renderer.effectBuffer.end();
					Vars.renderer.effectBuffer.blit(NHPShaders.range);
				});
				
				if(GII_Plugin.drawHighlight)Draw.draw(Layer.space + 10.54f, () -> {
					Draw.blend(Blending.additive);
					Vars.renderer.effectBuffer.begin(Color.clear);
					
					Lines.stroke(2f);
					us.each(entity -> drawer2.get(entity, entity.range(), entity.hitSize()));
					bs.each(entity -> drawer2.get(entity, ((BaseTurret.BaseTurretBuild)entity).range(), entity.hitSize()));
					
					Vars.renderer.effectBuffer.end();
					Vars.renderer.effectBuffer.blit(NHPShaders.fader);
					
					Draw.blend();
				});
			}
			
			Draw.blend();
			
			if(Core.settings.getBool(GII_Plugin.DRAW_UNIT_SIGN, true) && Vars.ui.hudfrag.shown){
				Seq<Unit> us2 = us.copy().retainAll(draw -> viewport.overlaps(draw.x() - draw.hitSize(), draw.y() - draw.hitSize(), draw.hitSize() * 2, draw.hitSize() * 2));
				
				us2.each(unit -> {
					Draw.z(Layer.light + 6);
					for(DrawPair<Unit> drawer : signDrawer){
						if(drawer.bool.get(unit)){
							drawer.drawer.get(unit);
							Draw.reset();
							break;
						}
					}
				});
			}
			
			Draw.reset();
			Draw.z(z);
		});
		
		
		
		Log.info("Plugin GII_EventListeners Added");
	}
}
