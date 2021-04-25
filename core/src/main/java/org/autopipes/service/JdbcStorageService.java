package org.autopipes.service;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.autopipes.model.AreaBody;
import org.autopipes.model.AreaCutSheet;
import org.autopipes.model.AreaOptions;
import org.autopipes.model.DrawingArea;
import org.autopipes.model.DrawingOptions;
import org.autopipes.model.FloorDrawing;
import org.autopipes.util.JaxbSqlParameterSource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
//import org.springframework.oxm.Marshaller;
//import org.springframework.oxm.Unmarshaller;

public class JdbcStorageService implements StorageService {
	  private static Logger logger = Logger.getLogger(JdbcStorageService.class);

      private Object unmarshalString(final ResultSet rs, final String col, Class<? extends Object> cls){
    	  String v = null;
    	  try{
    	      v = rs.getString(col);
    	  }catch(SQLException e){
    		  // column not part of this result set
    		  logger.info("Failed to get string for " + col);
    	  }
    	  if(v != null) {
        	  try {
  				JAXBContext context = JAXBContext.newInstance(cls);
  				Unmarshaller u = context.createUnmarshaller();
    	    	  Reader r = new StringReader(v);
    	    	  Source s = new StreamSource(r);
    	    	  return  u.unmarshal(s);    // unmarshaller.unmarshal(s);
        	  }catch(Exception e){
        		  logger.info("Failed string=" + v);
        		  logger.error("Failed to unmarshal " + col, e);
        	  }
    	  }
		  return null; // in case the column is not on the select list
      }
      
	  private final class DrawingMapper implements RowMapper<FloorDrawing> {
		    public FloorDrawing mapRow(final ResultSet rs, final int rowNum) throws SQLException {
		      FloorDrawing t = new FloorDrawing();
		      t.setId(rs.getBigDecimal("id").longValue());
		      t.setDwgName(rs.getString("name"));
		      Calendar cal = Calendar.getInstance();
		      cal.setTime(rs.getTimestamp("upd_date"));
		      t.setDwgUpdateDate(cal);
		      t.setDwgTextSize(rs.getBigDecimal("text_size").doubleValue());
		      DrawingOptions opt = (DrawingOptions)unmarshalString(rs, "configuration", DrawingOptions.class);
		      t.setOptionsRoot(opt);
			  return t;
			}
      }
	  private final class AreaMapper implements RowMapper<DrawingArea> {
		    public DrawingArea mapRow(final ResultSet rs, final int rowNum) throws SQLException {
		      DrawingArea t = new DrawingArea();
		      t.setDrawingId(rs.getBigDecimal("drawing_id").longValue());
		      t.setAreaId(rs.getBigDecimal("area_id").longValue());
		      t.setAreaName(rs.getString("area_name"));
		      t.setAreaReadiness(DrawingArea.Readiness.valueOf(rs.getString("area_readiness")));
              t.setDefectCount(rs.getInt("defect_count"));
		      AreaBody body = (AreaBody)unmarshalString(rs, "area", AreaBody.class);
		      t.setAreaBody(body);
		      AreaOptions options = (AreaOptions)unmarshalString(rs, "options", AreaOptions.class);
		      t.setAreaOptions(options);
		      AreaCutSheet cutSheet = (AreaCutSheet)unmarshalString(rs, "cut_sheet", AreaCutSheet.class);
		      t.setAreaCutSheet(cutSheet);
		      return t;
			}
      }

//      private Marshaller marshaller;
//      private Unmarshaller unmarshaller;
	  private DataSource dataSource;
	  private Resource resource;
	  private String schemaSeparator;
	  private NamedParameterJdbcTemplate npjt;
	  private JdbcTemplate sjt;

	  public void init() throws Exception{
			logger.info("+init");
			  File schemaFile = resource.getFile();
			  int length = (int)schemaFile.length();
			  String[] schema = new String[] {};
			  Reader reader = null;
			  try {
				  reader = new FileReader(schemaFile);
				  CharBuffer cb = CharBuffer.allocate(length);
				  reader.read(cb);
				  cb.position(0);
				  schema = cb.toString().split(schemaSeparator);
			  }finally {
				  if(reader != null) {
					  reader.close();
				  }
			  }
				for(String stmt : schema){
					if(stmt.trim().length() > 0){
						try {
						npjt.getJdbcOperations().execute(stmt.trim());
						} catch(DataAccessException ex){
							System.out.println("Expected except on 1st run: " + ex.getMessage());
						}
					}
				}
				logger.info("-init");
		  }

      public Long maxAreaId(final Long dwgId){
		  String sql = "SELECT MAX(area_id) FROM floor_area where drawing_id=?";
		  try {
			    Long ret = sjt.queryForObject(sql, new Object[] { dwgId}, Long.class);
			    return ret == null ? 0L : ret;
			  } catch (EmptyResultDataAccessException e){
				return 0L;
			  }
      }
      
	  public List<DrawingArea> findAllDrawingAreas(){
		  String sql = "SELECT drawing_id, area_id, area_name, area_readiness, defect_count  FROM floor_area";
		  RowMapper<DrawingArea> mapper = new AreaMapper();
		  return sjt.query(sql, mapper);
	  }

	  public List<DrawingArea> findDrawingAreas(final Long dwgId){
		  String sql = "SELECT drawing_id, area_id, area_name, area_readiness, defect_count  FROM floor_area where drawing_id=?";
		  RowMapper<DrawingArea> mapper = new AreaMapper();
		  return sjt.query(sql, new Object[] {dwgId}, mapper);
	  }

	  public List<FloorDrawing> findAllDrawings(){
		  String sql = "SELECT id, name, upd_date, text_size FROM floor_drawing";
		  RowMapper<FloorDrawing> mapper = new DrawingMapper();
		  return sjt.query(sql, mapper);
	  }
	  public FloorDrawing findOneDrawing(final Long id){
		  String sql = "SELECT * FROM floor_drawing where id=?";
		  RowMapper<FloorDrawing> mapper = new DrawingMapper();
		  try {
			      FloorDrawing ret = sjt.queryForObject(sql, new Object[] {id}, mapper);
			      return ret;
			  } catch (EmptyResultDataAccessException e){
				return null;
			  }
	  }
      public DrawingArea findOneDrawingArea(final Long dwgId, final Long areaId,
    		  boolean withBody, boolean withCutSheet){
		  String sql = "SELECT drawing_id, area_id, area_name, area_readiness, defect_count, options";
		  if(withBody){
			  sql += ", area";
		  }
		  if(withCutSheet){
			  sql += ", cut_sheet";
		  }
		  sql += " FROM floor_area where drawing_id=? and area_id=?";
		  RowMapper<DrawingArea> mapper = new AreaMapper();
		  try {
			  DrawingArea ret = sjt.queryForObject(sql, new Object[] {dwgId, areaId}, mapper);
			  if(ret.getAreaCutSheet() != null){
				  ret.getAreaCutSheet().postDeserialize();
			  }
			  return ret;
			  } catch (EmptyResultDataAccessException e){
				return null;
			  }
      }

	  public Long findDrawingId(final String name){
		  return findDrawingIdByProperty("name", name);
	  }
	  protected boolean isValidDrawingId(final Long id){
		  return findDrawingIdByProperty("id", id) != null;
	  }
	  protected boolean isValidAreaId(final Long drawingId, final Long areaId){
		  return findAreaIdByProperty(drawingId, "area_id", areaId) != null;
	  }
	  protected Long findDrawingIdByProperty(final String propertyName, final Object value){
		  String sql = "SELECT id FROM floor_drawing WHERE " + propertyName + "=?";

		  try {
		    return sjt.queryForObject(sql, new Object[] {value}, Long.class);
		  } catch (EmptyResultDataAccessException e){
			return null;
		  }
	  }
	  protected Long findAreaIdByProperty(final Long drawingId, final String propertyName, final Object value){
		  String sql = "SELECT area_id FROM floor_area WHERE drawing_id=? AND  " + propertyName + "=?";

		  try {
		    return sjt.queryForObject(sql, new Object[] {drawingId, value}, Long.class);
		  } catch (EmptyResultDataAccessException e){
			return null;
		  }
	  }
      public Long findAreaId(final Long drawingId, final String areaName){
          return findAreaIdByProperty(drawingId, "area_name", areaName);
      }

	  public void deleteDrawing(final Long id){
			String sql = "DELETE FROM floor_drawing WHERE id = ?";
		  	npjt.getJdbcOperations().update(sql, new Object[] { id });
	  }
	  public void deleteArea(final Long drawingId, final Long areaId){
			String sql = "DELETE FROM floor_area WHERE drawing_id=? AND area_id=?";
		  	npjt.getJdbcOperations().update(sql, new Object[] { drawingId,  areaId});
	  }
	  public void mergeDrawing(final FloorDrawing dwg)throws Exception{
		  if(dwg.getId() == null || !isValidDrawingId(dwg.getId())){
			  dwg.setId(findDrawingId(dwg.getDwgName()));
		  }
		  if(dwg.getOptionsRoot() != null){
			  dwg.getOptionsRoot().preSerialize();
		  }
		  JaxbSqlParameterSource ps = new JaxbSqlParameterSource(dwg);
		  String sql = null;
		  if(dwg.getId() == null){
			  KeyHolder kh = new GeneratedKeyHolder();
			  try {
	              sql = "INSERT INTO floor_drawing(name, text_size, upd_date, configuration)"
	  					+ "values(:dwgName, :dwgTextSize, :dwgUpdateDate, :optionsRoot)";
	    		      npjt.update(sql, ps, kh, new String[] {"id"});				  
			  }catch(Exception e) {
				  logger.info("insert error", e);
				  Long id = findDrawingId(dwg.getDwgName());
				  logger.info("drawing id is " + id);
				  if(id == null) {
					  throw new Exception("Please retry");
				  }
				  dwg.setId(id);
			  }
		  }else{
	          sql = "UPDATE floor_drawing SET"
	            	+ " upd_date = :dwgUpdateDate"
	            	+ ",text_size = :dwgTextSize"
	            	+ ",name = :dwgName";
	          if(dwg.getOptionsRoot() != null){
	              sql += ",configuration = :optionsRoot";
	          }
			  sql += " WHERE id = :id";
	  		  npjt.update(sql, ps);
		  }
	  }

	  public DrawingArea mergeArea(final DrawingArea area)throws Exception{
		  if(area.getDrawingId() == null){
			  throw new IllegalArgumentException("Missing drawing id");
		  }
		  boolean doInsert = false;
		  if(area.getAreaId() == null){
			  area.setAreaId(maxAreaId(area.getDrawingId()).longValue() + 1);
			  doInsert = true;
		  }
		  else if(!isValidAreaId(area.getDrawingId(), area.getAreaId())){
			  doInsert = true;
		  }
		  if(area.getAreaName() == null){
			  area.setAreaName("Area-" + area.getAreaId());
		  }
		  String sql = null;
		  if(doInsert){
              sql = "INSERT INTO floor_area"
            + "(drawing_id, area_id, area_name, options, area_readiness, defect_count, area)"
            + "values"
			+ "(:drawingId, :areaId, :areaName, :areaOptions, :areaReadiness, :defectCount, :areaBody)";
		  }else{
	          sql = "UPDATE floor_area SET area_name = :areaName";
	          if(area.getAreaReadiness() != null){
          	      sql += ",area_readiness = :areaReadiness, defect_count = :defectCount";
	          }
	          if(area.getAreaBody() != null){
	              sql += ",area = :areaBody";
	          }
	          if(area.getAreaOptions() != null){
	              sql += ",options = :areaOptions";
	          }
	          if(area.getAreaCutSheet() != null){
	        	  // compressing object tree
	        	  area.getAreaCutSheet().preSerialize();
	              sql += ",cut_sheet = :areaCutSheet";
	          }

			  sql += " WHERE area_id = :areaId AND drawing_id = :drawingId";
		  }
		  JaxbSqlParameterSource ps = new JaxbSqlParameterSource(area);
  		  npjt.update(sql, ps);
		  return area;
	  }

	  public void setDataSource(final DataSource dataSource) {
		    npjt = new NamedParameterJdbcTemplate(dataSource);
		    sjt = new JdbcTemplate(dataSource);
			this.dataSource = dataSource;
		  }
	  public void setSchema(final Resource resource) {
		  this.resource = resource;
	  }


	public String getSchemaSeparator() {
		return schemaSeparator;
	}


	public void setSchemaSeparator(final String separator) {
		schemaSeparator = separator;
	}

}
