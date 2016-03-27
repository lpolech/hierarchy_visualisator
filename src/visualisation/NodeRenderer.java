package visualisation;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import prefuse.render.AbstractShapeRenderer;
import prefuse.visual.VisualItem;
import utils.Constants;
import utils.ElementRole;
import utils.Parameters;

public class NodeRenderer extends AbstractShapeRenderer {
	//protected RectangularShape m_box = new Rectangle2D.Double();
    protected Ellipse2D m_box = new Ellipse2D.Double();
    protected int nodeSize;
    protected Parameters params;
    
	public NodeRenderer(int nodeSize, Parameters params) {
		// TODO Auto-generated constructor stub
		this.params = params;
		this.nodeSize = nodeSize; 
	}

	@Override
	protected Shape getRawShape(VisualItem item) {
		m_box.setFrame(item.getX(), item.getY(), nodeSize,nodeSize);//(Integer) item.get("age")/3, (Integer) item.get("age")/3);
		int role = item.getInt(Constants.PREFUSE_NODE_ROLE_COLUMN_NAME);
		//CURRENT, DIRECT_PARENT, INDIRECT_PARENT, CHILD, OTHER
		if(role == ElementRole.CURRENT.getNumber())
		{
			item.setFillColor(params.getCurrentLevelColor().getRGB());
		}
		else if(role == ElementRole.DIRECT_PARENT.getNumber())
		{
			item.setFillColor(params.getParentGroupsColor().getRGB());
		}
		else if(role == ElementRole.INDIRECT_PARENT.getNumber())
		{
			item.setFillColor(params.getParentAncestorsColor().getRGB());
		}
		else if(role == ElementRole.CHILD.getNumber())
		{
			item.setFillColor(params.getChildGroupsColor().getRGB());
		}
		else if(role == ElementRole.OTHER.getNumber())
		{
			item.setFillColor(params.getOtherGroupsColor().getRGB());
		}
		
		return m_box;
	}
	

}
