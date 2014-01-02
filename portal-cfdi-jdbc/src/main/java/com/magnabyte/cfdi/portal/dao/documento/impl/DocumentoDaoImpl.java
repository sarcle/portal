package com.magnabyte.cfdi.portal.dao.documento.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import mx.gob.sat.cfd._3.Comprobante;
import mx.gob.sat.timbrefiscaldigital.TimbreFiscalDigital;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.magnabyte.cfdi.portal.dao.GenericJdbcDao;
import com.magnabyte.cfdi.portal.dao.documento.DocumentoDao;
import com.magnabyte.cfdi.portal.dao.documento.sql.DocumentoSql;
import com.magnabyte.cfdi.portal.model.cliente.Cliente;
import com.magnabyte.cfdi.portal.model.documento.Documento;
import com.magnabyte.cfdi.portal.model.documento.DocumentoCorporativo;
import com.magnabyte.cfdi.portal.model.documento.DocumentoSucursal;
import com.magnabyte.cfdi.portal.model.documento.EstadoDocumentoPendiente;
import com.magnabyte.cfdi.portal.model.documento.TipoDocumento;
import com.magnabyte.cfdi.portal.model.establecimiento.Establecimiento;
import com.magnabyte.cfdi.portal.model.establecimiento.RutaRepositorio;
import com.magnabyte.cfdi.portal.model.exception.PortalException;

@Repository("documentoDao")
public class DocumentoDaoImpl extends GenericJdbcDao implements DocumentoDao {

	private static final Logger logger = 
			LoggerFactory.getLogger(DocumentoDaoImpl.class);
	
	@Override
	public void save(Documento documento) {
		try {
			SimpleJdbcInsert simpleInsert = new SimpleJdbcInsert(getJdbcTemplate());
			simpleInsert.setTableName(DocumentoSql.TABLE_NAME);
			simpleInsert.setGeneratedKeyName(DocumentoSql.ID_DOCUMENTO);
			documento.setId(simpleInsert.executeAndReturnKey(getParameters(documento)).intValue());
		} catch (DataAccessException ex) {			
			logger.debug("No se pudo registrar el Documento en la base de datos.", ex);
			throw new PortalException("No se pudo registrar el Documento en la base de datos.", ex);
		}
	}

	private MapSqlParameterSource getParameters(Documento documento) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(DocumentoSql.ID_ESTABLECIMIENTO, documento.getEstablecimiento().getId());
		params.addValue(DocumentoSql.ID_CLIENTE, documento.getCliente().getId());
		if(documento instanceof DocumentoSucursal) {
			params.addValue(DocumentoSql.ID_TICKET, ((DocumentoSucursal) documento)
					.getTicket().getId());
		} else if(documento instanceof DocumentoCorporativo){
			params.addValue(DocumentoSql.FOLIO_SAP, ((DocumentoCorporativo) documento).getFolioSap());
		}
		params.addValue(DocumentoSql.FECHA_DOCUMENTO, new Date());
		params.addValue(DocumentoSql.TOTAL_DESCUENTO, documento.getComprobante().getDescuento());
		params.addValue(DocumentoSql.SUBTOTAL, documento.getComprobante().getSubTotal());
		params.addValue(DocumentoSql.IVA, documento.getComprobante().getImpuestos().getTotalImpuestosTrasladados());
		params.addValue(DocumentoSql.TOTAL, documento.getComprobante().getTotal());
		params.addValue(DocumentoSql.STATUS, true);
		
		return params;
	}

	@Override
	public void insertDocumentoFolio(Documento documento) {
		SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(getJdbcTemplate());
		simpleJdbcInsert.setTableName(DocumentoSql.TABLE_DOC_FOLIO);
		simpleJdbcInsert.execute(getParametersDocumentoFolio(documento));
	}
	
	private MapSqlParameterSource getParametersDocumentoFolio(Documento documento) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(DocumentoSql.ID_DOCUMENTO, documento.getId());
		params.addValue(DocumentoSql.SERIE, documento.getComprobante().getSerie());
		params.addValue(DocumentoSql.FOLIO, documento.getComprobante().getFolio());
		params.addValue(DocumentoSql.ID_TIPO_DOCUMENTO, documento.getTipoDocumento().getId());
		return params;
	}

	@Override
	public void insertDocumentoCfdi(Documento documento) {
		SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(getJdbcTemplate());
		simpleJdbcInsert.setTableName(DocumentoSql.TABLE_DOC_CFDI);
		simpleJdbcInsert.execute(getParametersDocumentoCfdi(documento));
	}
	
	private MapSqlParameterSource getParametersDocumentoCfdi(Documento documento) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(DocumentoSql.ID_DOCUMENTO, documento.getId());
		params.addValue(DocumentoSql.SELLO_EMISOR, documento.getTimbreFiscalDigital().getSelloSAT());
		params.addValue(DocumentoSql.CADENA, documento.getCadenaOriginal());
		params.addValue(DocumentoSql.SELLO_CFDI, documento.getTimbreFiscalDigital().getSelloCFD());
		params.addValue(DocumentoSql.UUID, documento.getTimbreFiscalDigital().getUUID());
		//FIXME
		params.addValue(DocumentoSql.FECHA_HORA, new Date());
		return params;
	}
	
	@Override
	public void insertAcusePendiente(Documento documento) {
		SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(getJdbcTemplate());
		simpleJdbcInsert.setTableName(DocumentoSql.TABLE_DOC_PEND);
		simpleJdbcInsert.execute(getParametersAcusePendiente(documento));
	}

	private MapSqlParameterSource getParametersAcusePendiente(Documento documento) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(DocumentoSql.ID_DOCUMENTO, documento.getId());
		params.addValue(DocumentoSql.SERIE, documento.getComprobante().getSerie());
		params.addValue(DocumentoSql.FOLIO, documento.getComprobante().getFolio());
		params.addValue(DocumentoSql.ID_TIPO_DOCUMENTO, documento.getTipoDocumento().getId());
		params.addValue(DocumentoSql.ID_ESTABLECIMIENTO, documento.getEstablecimiento().getId());
		params.addValue(DocumentoSql.ID_ESTADO_DOC, EstadoDocumentoPendiente.ACUSE_PENDIENTE.getId());
		return params;
	}
	
	@Override
	public void deleteFromAcusePendiente(Documento documento) {
		getJdbcTemplate().update("delete from t_documento_pendiente where id_documento = ?", documento.getId());
	}
	
	@Override
	public List<Documento> obtenerAcusesPendientes() {
		try {
			return getJdbcTemplate().query(DocumentoSql.READ_ACUSE_PEND, new RowMapper<Documento>() {
				@Override
				public Documento mapRow(ResultSet rs, int rowNum) throws SQLException {
					Documento documento = new Documento();
					TimbreFiscalDigital timbreFiscalDigital = new TimbreFiscalDigital();
					Establecimiento establecimiento = new Establecimiento();
					Comprobante comprobante = new Comprobante();
					
					documento.setId(rs.getInt(1));
					TipoDocumento tipoDocumento = rs.getInt("id_tipo_documento") == 1 ? TipoDocumento.FACTURA : TipoDocumento.NOTA_CREDITO;
					documento.setTipoDocumento(tipoDocumento);
					comprobante.setSerie(rs.getString("serie"));
					comprobante.setFolio(rs.getString("folio"));
					establecimiento.setId(rs.getInt("id_establecimiento"));
					timbreFiscalDigital.setUUID(rs.getString("uuid"));
					documento.setTimbreFiscalDigital(timbreFiscalDigital);
					documento.setComprobante(comprobante);
					documento.setEstablecimiento(establecimiento);
					return documento;
				}
			});
		} catch (EmptyResultDataAccessException ex) {
			logger.debug("No hay acuses pendientes");
			return null;
		}
	}

	@Override
	public List<Documento> getNombreDocumento(List<Integer> idDocumentos) {
		MapSqlParameterSource map = new MapSqlParameterSource();
		map.addValue("idDocumentos", idDocumentos);
		return getNamedParameterJdbcTemplate().query(DocumentoSql.READ_DOCUMENTO, map, DOCUMENTO_MAPPER);
	}
	
	private static final RowMapper<Documento> DOCUMENTO_MAPPER = new RowMapper<Documento>() {

		@Override
		public Documento mapRow(ResultSet rs, int rowNum) throws SQLException {
			Documento documento = new Documento();
			Comprobante comprobante = new Comprobante();
			comprobante.setFolio(rs.getString("folio"));
			comprobante.setSerie(rs.getString("serie"));
			documento.setId(rs.getInt("id_documento"));
			documento.setTipoDocumento(TipoDocumento.FACTURA);
			documento.setComprobante(comprobante);
			return documento;
		}
	};
	
	private static final RowMapper<Documento> DOCUMENTO_RUTA_MAPPER = new RowMapper<Documento>() {
		@Override
		public Documento mapRow(ResultSet rs, int rowNum) throws SQLException {
			Documento documento = new Documento();
			Establecimiento establecimiento = new Establecimiento();
			RutaRepositorio ruta = new RutaRepositorio();
			
			
			ruta.setRutaRepositorio(rs.getString("ruta_repo"));
			ruta.setRutaRepoOut(rs.getString("ruta_out"));
			
			establecimiento.setRutaRepositorio(ruta);			
			documento.setEstablecimiento(establecimiento);
			documento.setId(rs.getInt("id_documento"));
			return documento;
		}
	};

	@Override
	public List<Documento> getDocumentoByCliente(Cliente cliente) {		
		return getJdbcTemplate().query(DocumentoSql.READ_DOCUMENTO_RUTA, DOCUMENTO_RUTA_MAPPER, cliente.getRfc());
	}
}
