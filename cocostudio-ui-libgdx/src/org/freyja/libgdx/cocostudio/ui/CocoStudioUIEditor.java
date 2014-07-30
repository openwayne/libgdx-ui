package org.freyja.libgdx.cocostudio.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freyja.libgdx.cocostudio.ui.model.CCExport;
import org.freyja.libgdx.cocostudio.ui.model.CCOption;
import org.freyja.libgdx.cocostudio.ui.model.CCWidget;
import org.freyja.libgdx.cocostudio.ui.model.animation.CCAction;
import org.freyja.libgdx.cocostudio.ui.model.animation.CCActionFrame;
import org.freyja.libgdx.cocostudio.ui.model.animation.CCActionNode;
import org.freyja.libgdx.cocostudio.ui.model.animation.CCAnimation;
import org.freyja.libgdx.cocostudio.ui.parser.group.CCButton;
import org.freyja.libgdx.cocostudio.ui.parser.group.CCCheckBox;
import org.freyja.libgdx.cocostudio.ui.parser.group.CCLabelAtlas;
import org.freyja.libgdx.cocostudio.ui.parser.group.CCListView;
import org.freyja.libgdx.cocostudio.ui.parser.group.CCPageView;
import org.freyja.libgdx.cocostudio.ui.parser.group.CCPanel;
import org.freyja.libgdx.cocostudio.ui.parser.group.CCScrollView;
import org.freyja.libgdx.cocostudio.ui.parser.widget.CCImageView;
import org.freyja.libgdx.cocostudio.ui.parser.widget.CCLabel;
import org.freyja.libgdx.cocostudio.ui.parser.widget.CCLabelBMFont;
import org.freyja.libgdx.cocostudio.ui.parser.widget.CCLoadingBar;
import org.freyja.libgdx.cocostudio.ui.parser.widget.CCSlider;
import org.freyja.libgdx.cocostudio.ui.parser.widget.CCTextField;
import org.freyja.libgdx.cocostudio.ui.res.TextureManager;
import org.freyja.libgdx.cocostudio.ui.widget.TTFLabelStyle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SnapshotArray;

/**
 * CocoStudio ui 解析器.根据CocoStudio的ui编辑器生成的json文件,创建出一个对应Group.
 * 本解析器还处于初级阶段,部分控件与属性不支持.
 * 
 * @author i see
 * @email 121077313@qq.com
 * @wiki https://github.com/121077313/cocostudio-ui-libgdx/wiki
 * @tip https://github.com/121077313/cocostudio-ui-libgdx/wiki/疑难解答
 */
public class CocoStudioUIEditor implements Disposable {
	private static int regTag = 0;
	private String regFileName;

	private Group editorDos;

	final String tag = CocoStudioUIEditor.class.getName();

	/** json文件所在目录 */
	protected String dirName = "";

	/** 所有纹理 */
	protected Collection<TextureAtlas> textureAtlas;

	/** 控件集合 */
	protected Map<String, Array<Actor>> actors;

	protected Map<Integer, Actor> actionActors;

	Map<String, Map<Actor, Action>> animations;

	/** 字体集合 */
	protected Map<String, FileHandle> ttfs;

	/** BitmapFont集合,key:font.fnt */
	protected Map<String, BitmapFont> bitmapFonts;

	/** 导出的json结构 */
	protected CCExport export;

	protected Map<String, BaseWidgetParser> parsers;

	/** 添加转换器 */
	public void addParser(BaseWidgetParser parser) {
		parsers.put(parser.getClassName(), parser);
	}

	/** 默认ttf字体文件 */
	protected FileHandle defaultFont;

	private Map<Integer, Actor> _tags;

	/**
	 * 不需要显示文字
	 * 
	 * @param jsonFile
	 * @param textureAtlas
	 *            资源文件,传入 null表示使用小文件方式加载图片
	 */
	public CocoStudioUIEditor(FileHandle jsonFile,
			Collection<TextureAtlas> textureAtlas) {
		this(jsonFile, null, null, null, textureAtlas);
	}

	/**
	 * 
	 * @param jsonFile
	 *            ui编辑成生成的json文件
	 * @param textureAtlas
	 *            资源文件,传入 null表示使用小文件方式加载图片.
	 * 
	 * @param ttfs
	 *            字体文件集合
	 * @param bitmapFonts
	 *            自定义字体文件集合
	 * @param defaultFont
	 *            默认ttf字体文件
	 */
	public CocoStudioUIEditor(FileHandle jsonFile,
			Map<String, FileHandle> ttfs, Map<String, BitmapFont> bitmapFonts,
			FileHandle defaultFont, Collection<TextureAtlas> textureAtlas) {

		regFileName = jsonFile.name() + "|id:" + regTag++;

		this.textureAtlas = textureAtlas;
		this.ttfs = ttfs;
		this.bitmapFonts = bitmapFonts;
		this.defaultFont = defaultFont;
		parsers = new HashMap<String, BaseWidgetParser>();

		addParser(new CCButton());
		addParser(new CCCheckBox());
		addParser(new CCImageView());
		addParser(new CCLabel());
		addParser(new CCLabelBMFont());
		addParser(new CCPanel());
		addParser(new CCScrollView());
		addParser(new CCTextField());
		addParser(new CCLoadingBar());

		addParser(new CCLabelAtlas());
		addParser(new CCSlider());

		addParser(new CCPageView());
		addParser(new CCListView());

		actors = new HashMap<String, Array<Actor>>();
		actionActors = new HashMap<Integer, Actor>();
		_tags = new HashMap<Integer, Actor>();

		animations = new HashMap<String, Map<Actor, Action>>();

		// // 改造一下,方便资源统一管理
		// dirName = jsonFile.parent().toString();
		//
		// if (!dirName.equals("")) {
		// dirName += File.separator;
		// }
		String json = jsonFile.readString("utf-8");
		Json jj = new Json();
		jj.setIgnoreUnknownFields(true);
		export = jj.fromJson(CCExport.class, json);

	}

	/**
	 * 根据控件名字查找Actor
	 * 
	 * @param name
	 *            控件名字
	 * @return
	 */
	public Actor findActor(String name) {
		Array<Actor> array = actors.get(name);
		if (array == null || array.size == 0) {
			return null;
		}
		return array.get(0);
	}

	public Actor findActorByTag(int tag) {
		return _tags.get(tag);
	}

	/** 查找所有同名的控件 */
	public Array<Actor> findActors(String name) {

		return actors.get(name);
	}

	/**
	 * 根据json文件创建并返回Group
	 * 
	 * @return
	 */
	public Group createGroup() {
		if (editorDos == null) {
			editorDos = (Group) parseWidget(null, export.getWidgetTree());
		}
		parseAction();
		return editorDos;
	}

	/** 查找动画 */
	public Map<Actor, Action> getAction(String animationName) {

		return animations.get(animationName);
	}

	/** 转换动作Action */
	void parseAction() {

		CCAnimation animation = export.getAnimation();
		for (CCAction action : animation.getActionlist()) {

			List<CCActionNode> nodes = action.getActionnodelist();
			Map<Actor, Action> actions = new HashMap<Actor, Action>();
			for (CCActionNode node : nodes) {
				Actor actor = actionActors.get(node.getActionTag());
				List<CCActionFrame> frames = node.getActionframelist();
				// frameid 排序.
				SequenceAction sequenceAction = Actions.sequence();
				for (CCActionFrame frame : frames) {
					Interpolation interpolation = getInterpolation(frame
							.getTweenType());

					float duration = 0;
					// Starttime 会是一个类似
					// 7.163279E-39的字符没办法直接转换Float,所以这里采用截取字符串的方法
					int length = frame.getStarttime().indexOf("E");
					if (length != -1) {
						duration = Float.parseFloat(frame.getStarttime()
								.substring(0, length));
					} else {
						duration = Float.parseFloat(frame.getStarttime());
					}

					Action moveTo = Actions.moveTo(frame.getPositionx(),
							frame.getPositiony(), duration, interpolation);

					Action scaleTo = Actions.scaleTo(frame.getScalex(),
							frame.getScaley(), duration, interpolation);

					Action color = Actions.color(new Color(
							frame.getColorr() / 255.0f,
							frame.getColorg() / 255.0f,
							frame.getColorb() / 255.0f,
							frame.getOpacity() / 255.0f), duration,
							interpolation);

					Action rotateTo = Actions.rotateTo(frame.getRotation(),
							duration, interpolation);

					sequenceAction.addAction(Actions.parallel(moveTo, scaleTo,
							color, rotateTo));
				}

				actions.put(actor, sequenceAction);
			}

			animations.put(action.getName(), actions);
		}

	}

	public Interpolation getInterpolation(int tweenType) {
		return null;
	}

	protected TextureRegion findRegion(String name) {
		for (TextureAtlas ta : textureAtlas) {
			TextureRegion tr = ta.findRegion(name);
			if (tr != null) {
				return tr;
			}
		}
		return null;
	}

	protected TextureRegion findRegion(String name, int index) {
		for (TextureAtlas ta : textureAtlas) {
			TextureRegion tr = ta.findRegion(name, index);
			if (tr != null) {
				return tr;
			}
		}
		return null;
	}

	/**
	 * 获取材质
	 * 
	 * @param option
	 * @param name
	 * @return
	 */
	public TextureRegion findTextureRegion(CCOption option, String name) {
		if (name == null || name.equals("")) {
			return null;
		}
		TextureRegion tr = null;
		if (textureAtlas == null || textureAtlas.size() == 0) {// 不使用合并纹理
			tr = new TextureRegion(TextureManager.getTexture(regFileName,
					Gdx.files.internal(dirName + name)));
		} else {

			try {
				String[] arr = name.split("\\/");
				String newName = arr[arr.length - 1];
				newName = arr[arr.length - 1].substring(0, newName.length() - 4);
				tr = findRegion(newName);

			} catch (Exception e) {
				tr = findRegion(name);
			}
		}
		if (tr == null) {
			debug(option, "找不到纹理");
		}

		if (option.isFlipX() || option.isFlipY()) {

			if (textureAtlas == null) {
				tr.flip(option.isFlipX(), option.isFlipY());
			} else {
				tr = new TextureRegion(tr);
				tr.flip(option.isFlipX(), option.isFlipY());
			}
		}

		return tr;
	}

	/**
	 * .9文件生成
	 * 
	 * @param option
	 * @param name
	 * @author wujj
	 * @return
	 */
	public NinePatch findNinePatch(CCOption option, String name) {
		if (name == null || name.equals("")) {
			return null;
		}

		NinePatch tr = null;
		if (textureAtlas == null || textureAtlas.size() == 0) {// 不使用合并纹理
			tr = new NinePatch(TextureManager.getTexture(regFileName,
					Gdx.files.internal(dirName + name)),
					option.getCapInsetsX(), option.getCapInsetsX()
							+ option.getCapInsetsWidth(),
					option.getCapInsetsY(), option.getCapInsetsY()
							+ option.getCapInsetsHeight());
		} else {
			name = name.substring(name.lastIndexOf("/") + 1, name.indexOf("."));

			for (TextureAtlas atlas : textureAtlas) {
				TextureRegion trg = atlas.findRegion(name);
				tr = new NinePatch(trg, option.getCapInsetsX(),
						option.getCapInsetsX() + option.getCapInsetsWidth(),
						option.getCapInsetsY(), option.getCapInsetsY()
								+ option.getCapInsetsHeight());
				// 这么用绝逼出错
				// tr = atlas.createPatch(name);
				if (tr != null) {
					break;
				}
			}
		}
		if (tr == null) {
			debug(option, "找不到纹理");
		}
		// 不支持翻转和镜像
		return tr;
	}

	public Drawable findDrawable(CCOption option, String name) {
		if (option.isScale9Enable() || option.isBackGroundScale9Enable()) {// 九宫格支持
			NinePatch np = findNinePatch(option, name);
			if (np == null) {
				return null;
			}
			return new NinePatchDrawable(np);
		}

		TextureRegion tr = findTextureRegion(option, name);
		if (tr == null) {
			return null;
		}

		return new TextureRegionDrawable(tr);
	}

	public void debug(String message) {
		Gdx.app.debug(tag, message);
	}

	public void debug(CCOption option, String message) {
		Gdx.app.debug(tag, "控件: " + option.getName() + " " + message);
	}

	public void error(String message) {
		Gdx.app.error(tag, message);
	}

	public void error(CCOption option, String message) {
		Gdx.app.error(tag, "控件: " + option.getName() + " " + message);
	}

	/***
	 * 解析节点,创建控件
	 * 
	 * @param node
	 * @return
	 */
	public Actor parseWidget(Group parent, CCWidget widget) {

		CCOption option = widget.getOptions();
		String className = option.getClassname();
		BaseWidgetParser parser = parsers.get(className);

		if (parser == null) {

			debug(option, "not support Widget:" + className);
			return null;
		}
		Actor actor = parser.parse(this, widget, option);

		actor = parser.commonParse(this, widget, option, parent, actor);

		return actor;

	}

	/** 获取BitmapFont */
	public BitmapFont getBitmapFont(CCOption option) {
		BitmapFont font = null;
		if (bitmapFonts != null) {
			font = bitmapFonts.get(option.getFileNameData().getPath());
		} else {
			font = new BitmapFont(Gdx.files.internal(dirName
					+ option.getFileNameData().getPath()));
		}

		if (font == null) {
			debug(option, "BitmapFont字体:" + option.getFileNameData().getPath()
					+ " 不存在");
			font = new BitmapFont();
		}
		return font;
	}

	/**
	 * 创建LabelStyle
	 * 
	 * @param option
	 * @return
	 */
	public TTFLabelStyle createLabelStyle(CCOption option) {

		FileHandle fontFile = null;
		if (ttfs != null) {
			fontFile = ttfs.get(option.getFontName());
		}

		if (fontFile == null) {// 使用默认字体文件
			fontFile = defaultFont;
		}

		if (fontFile == null) {
			fontFile = Gdx.files.internal("DroidSansFallback.ttf");
			debug(option, "ttf字体:" + option.getFontName() + " 不存在,使用默认字体");
		}

		return new TTFLabelStyle(Color.WHITE, fontFile, option.getFontSize());
	}

	public Map<String, Array<Actor>> getActors() {
		return actors;
	}

	public String getDirName() {
		return dirName;
	}

	public void setDirName(String dirName) {
		this.dirName = dirName;
	}

	public Map<String, FileHandle> getTtfs() {
		return ttfs;
	}

	public void setTtfs(Map<String, FileHandle> ttfs) {
		this.ttfs = ttfs;
	}

	public Map<String, BitmapFont> getBitmapFonts() {
		return bitmapFonts;
	}

	public void setBitmapFonts(Map<String, BitmapFont> bitmapFonts) {
		this.bitmapFonts = bitmapFonts;
	}

	public void setActors(Map<String, Array<Actor>> actors) {
		this.actors = actors;
	}

	public Map<Integer, Actor> getActionActors() {
		return actionActors;
	}

	public void setActionActors(Map<Integer, Actor> actionActors) {
		this.actionActors = actionActors;
	}

	public Map<Integer, Actor> getTags() {
		return _tags;
	}

	private void disposeGroup(Group group) {
		SnapshotArray<Actor> childArr = group.getChildren();
		for (int i = 0; i < childArr.size; i++) {
			Actor tmp = childArr.get(i);
			if (tmp instanceof Disposable) {
				((Disposable) tmp).dispose();
			} else if (tmp instanceof Group) {
				disposeGroup((Group) tmp);
			}
		}
	}

	@Override
	public void dispose() {
		disposeGroup(editorDos);
		TextureManager.cleanModule(regFileName);
	}

}
