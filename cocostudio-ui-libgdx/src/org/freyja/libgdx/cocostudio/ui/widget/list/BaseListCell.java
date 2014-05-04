package org.freyja.libgdx.cocostudio.ui.widget.list;

import org.freyja.libgdx.cocostudio.ui.widget.ListView;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;

/**
 * 列表里的item
 * 
 * @author jianxiang.zi
 * 
 */
public abstract class BaseListCell extends Table implements ICellModel {
	protected Object data;
	protected int index;
	private Image selectedImage;
	private int width;
	private int height;
	/**
	 * 被选中图片要不要被拉伸(圆形的是不能拉伸的)
	 */
	private boolean stretch;
	protected boolean selected;
	protected Array<Object> params;

	public BaseListCell() {
		this(50, 50, null, false);
	}
	
	public BaseListCell(int w, int h) {
		this(w, h, null, false);
	}
	
	public BaseListCell(int w, int h, Drawable selectbg, boolean stretch) {
		width = w;
		height = h;
		if (selectbg != null) {
			selectedImage = new Image(selectbg);
			selectedImage.setTouchable(Touchable.disabled);
		}
		this.stretch = stretch;
		setupListeners();
		params = new Array<Object>();
		this.setSize(width, height);
	}

	public void setCellSize(int w, int h) {
		width = w;
		height = h;
		this.setSize(width, height);
	}

	public int getCellWidth() {
		return width;
	}

	public int getCellHeight() {
		return height;
	}

	public void dispose() {

	}

	private void setupListeners() {
		addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				if (BaseListCell.this.hasParent()) {
					Group parent = BaseListCell.this.getParent();
					if (parent instanceof ListView) {
						((ListView) parent).cellClicked(BaseListCell.this);
					}
				}
				onClicked(event, x, y);
			}
		});
	}

	protected void onClicked(InputEvent event, float x, float y) {

	}

	public void setData(Object data) {
		this.data = data;
		if (data == null) {
			initNoneDataUI();
		} else {
			initUI();
		}
		initSelectedImage();
	}

	/**
	 * 没数据时候的处理
	 */
	protected void initNoneDataUI() {

	}

	protected void initUI() {

	}

	protected void afterAllCellsAdded() {

	}

	protected void initSelectedImage() {
		if (selectedImage != null) {
			selectedImage.setVisible(false);
			if (stretch) {
				selectedImage.setSize(width, height);
			}
			addActor(selectedImage);
		}
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		if (selectedImage != null) {
			selectedImage.setVisible(selected);
		}
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return this.index;
	}

	public void setParams(Array<Object> params) {
		this.params.clear();
		this.params.addAll(params);
	}

	public Image getSelectedImage() {
		return selectedImage;
	}

	public Object getData() {
		return data;
	}

}
