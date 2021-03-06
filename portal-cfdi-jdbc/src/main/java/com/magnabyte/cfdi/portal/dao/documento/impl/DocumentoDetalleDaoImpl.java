package com.magnabyte.cfdi.portal.dao.documento.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.magnabyte.cfdi.portal.dao.GenericJdbcDao;
import com.magnabyte.cfdi.portal.dao.documento.DocumentoDetalleDao;
import com.magnabyte.cfdi.portal.dao.documento.sql.DocumentoDetalleSql;
import com.magnabyte.cfdi.portal.model.cfdi.v32.Comprobante.Conceptos;
import com.magnabyte.cfdi.portal.model.cfdi.v32.Comprobante.Conceptos.Concepto;
import com.magnabyte.cfdi.portal.model.documento.Documento;
import com.magnabyte.cfdi.portal.model.exception.PortalException;

/**
 * 
 * @author Magnabyte, S.A. de C.V
 * magnabyte.com.mx
 * Fecha:27/01/2014
 * Clase que representa el acceso a datos de detalle de documento
 */
@Repository("documentoDetalleDao")
public class DocumentoDetalleDaoImpl extends GenericJdbcDao
	implements DocumentoDetalleDao {
	
	private static final Logger logger = 
			LoggerFactory.getLogger(DocumentoDetalleDaoImpl.class);
	
	@Autowired
	private MessageSource messageSource;

	public void save(final Documento documento) {
		String qry = DocumentoDetalleSql.INSERT_DETALLE_DOC;

		try {
			getJdbcTemplate().batchUpdate(qry,new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i)
						throws SQLException {
					Concepto conceptos = documento.getComprobante()
							.getConceptos().getConcepto().get(i);
					
					ps.setInt(1, documento.getId());
					ps.setBigDecimal(2, conceptos.getImporte());
					ps.setBigDecimal(3, conceptos.getValorUnitario());
					ps.setString(4, conceptos.getDescripcion());
					ps.setString(5, conceptos.getUnidad());
					ps.setBigDecimal(6, conceptos.getCantidad());
				}

				@Override
				public int getBatchSize() {
					return documento.getComprobante().getConceptos()
							.getConcepto().size();
				}
			});
			
		} catch (DataAccessException ex) {
			logger.debug(messageSource.getMessage("documento.detalle.error.save", new Object[] {ex}, null));
			throw new PortalException(messageSource.getMessage("documento.detalle.error.save", new Object[] {ex}, null));
		}
	}
	
	public Conceptos read(Documento documento) {
		return getJdbcTemplate().queryForObject(
				DocumentoDetalleSql.READ_DOC_DETALLE, DOC_DETALLE_MAPPER, documento.getId());
	}
	
	private static final RowMapper<Conceptos> DOC_DETALLE_MAPPER = new RowMapper<Conceptos>() {

		@Override
		public Conceptos mapRow(ResultSet rs, int rowNum) throws SQLException {
			Conceptos conceptos = new Conceptos();
			Concepto concepto = new Concepto();
			
			concepto.setCantidad(rs.getBigDecimal(DocumentoDetalleSql.CANTIDAD));
			concepto.setUnidad(rs.getString(DocumentoDetalleSql.UNIDAD));
			concepto.setDescripcion(rs.getString(DocumentoDetalleSql.DESCRIPCION));
			concepto.setValorUnitario(rs.getBigDecimal(DocumentoDetalleSql.PRECIO_UNITARIO));
			concepto.setImporte(rs.getBigDecimal(DocumentoDetalleSql.PRECIO_TOTAL));
			
			conceptos.getConcepto().add(concepto);
			
			return conceptos;
		}
	};
}
