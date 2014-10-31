/*
 ** 2014 October 02
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.gui.model;

import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.Reference;
import info.ata4.unity.gui.util.FieldNodeUtils;
import info.ata4.unity.asset.FieldTypeNode;
import info.ata4.unity.rtti.FieldTypeNodeComparator;
import info.ata4.unity.rtti.ObjectData;
import info.ata4.unity.rtti.RuntimeTypeException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetFileNode extends DefaultMutableTreeNode {
    
    private static final Logger L = LogUtils.getLogger();
    
    private final JTree tree;
    
    public AssetFileNode(JTree tree, AssetFile assetFile) {
        super(assetFile);
        
        this.tree = tree;
        
        if (!assetFile.isStandalone()) {
            addTypes(assetFile);
        }
        
        addObjects(assetFile);
        addReferences(assetFile);
    }
    
    private void addObjects(AssetFile asset) {
        Map<String, DefaultMutableTreeNode> nodeCategories = new TreeMap<>();
        for (ObjectData objectData : asset.getObjects()) {
            try {
                String fieldNodeType = objectData.getTypeTree().getType().getTypeName();

                if (!nodeCategories.containsKey(fieldNodeType)) {
                    DefaultMutableTreeNode nodeCategory = new DefaultMutableTreeNode(fieldNodeType);
                    nodeCategories.put(fieldNodeType, nodeCategory);
                }

                nodeCategories.get(fieldNodeType).add(new ObjectDataNode(tree, objectData));
            } catch (RuntimeTypeException ex) {
                L.log(Level.WARNING, "Can't deserialize object " + objectData, ex);
                add(new DefaultMutableTreeNode(ex));
            }
        }

        DefaultMutableTreeNode objectNode = new DefaultMutableTreeNode("Objects");

        for (DefaultMutableTreeNode treeNode : nodeCategories.values()) {
            objectNode.add(treeNode);
        }

        add(objectNode);
    }
    
    private void addReferences(AssetFile asset) {
        if (asset.getReferences().isEmpty()) {
            return;
        }
        
        DefaultMutableTreeNode refNode = new DefaultMutableTreeNode("References");
        
        for (Reference ref : asset.getReferences()) {
            refNode.add(new DefaultMutableTreeNode(ref));
        }
        
        add(refNode);
    }
    
    private void addTypes(AssetFile asset) {
        if (asset.isStandalone()) {
            return;
        }
        
        DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode("Types");
        Set<FieldTypeNode> fieldTypeNodes = new TreeSet<>(new FieldTypeNodeComparator());
        fieldTypeNodes.addAll(asset.getTypeTree().getFields().values());
        
        for (FieldTypeNode fieldNode : fieldTypeNodes) {
            FieldNodeUtils.convertFieldTypeNode(typeNode, fieldNode);
        }
 
        add(typeNode);
    }
}
