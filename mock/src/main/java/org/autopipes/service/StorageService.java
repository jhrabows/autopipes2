package org.autopipes.service;

import java.util.List;

import org.autopipes.model.DrawingArea;
import org.autopipes.model.FloorDrawing;

public interface StorageService {
 	List<FloorDrawing> findAllDrawings();
 	FloorDrawing findOneDrawing(Long id);
 	Long findDrawingId(String name);
 	/**
 	 * Inserts or updates floor_drawing table depending on the presence of dwg.id.
 	 * @param dwg
 	 * @throws Exception
 	 */
 	void mergeDrawing(FloorDrawing dwg) throws Exception;
 	
	void deleteDrawing(Long id);
	
	
 	/**
 	 * Inserts or updates floor_area table depending on the presence of area.areaId.
 	 * @param area
 	 * @throws Exception
 	 */
 	DrawingArea mergeArea(DrawingArea area) throws Exception;
 	
 	/**
 	 * Returns summary information for all areas.
 	 * @return the summary records
 	 */
 	List<DrawingArea> findAllDrawingAreas();

 	/**
 	 * Returns summary information for all areas associated with a drawing id.
 	 * @param dwgId the drawing id
 	 * @return collection of the summary records
 	 */
 	List<DrawingArea> findDrawingAreas(Long dwgId);
 	/**
 	 * Returns detailed information about a particular area in a drawing
 	 * @param dwgId id of the dreawing
 	 * @param areaId id of the area
 	 * @param withBody if <code>true</code> - include xml extracted from AutoCAD
 	 * @param withCutSheet if <code>true</code> - include cut-sheet xml report
 	 * @return the DrawingArea object
 	 */
    DrawingArea findOneDrawingArea(final Long dwgId, final Long areaId,
  		  boolean withBody, boolean withCutSheet);
 	Long findAreaId(Long drawingId, String areaName);
	void deleteArea(Long drawingId, Long areaId);
    Long maxAreaId(Long dwgId);
}
