package com.magnabyte.cfdi.portal.dao.commons.sql;

import com.magnabyte.cfdi.portal.dao.GenericSql;
import com.magnabyte.cfdi.portal.dao.establecimiento.sql.EstablecimientoSql;

public class UsuarioSql extends GenericSql {

	public static final String TABLE_NAME = "t_usuario";
	
	public static final String ID_USUARIO = "id_usuario";
	public static final String USUARIO = "usuario";
	public static final String PASSWORD = "password";
	
	public static final String GET_BY_ESTABLECIMIENTO;
	
	static {
		StringBuilder qryBuilder = new StringBuilder();
		
		qryBuilder.append(SELECT).append(EOL).append(TAB);
		qryBuilder.append(ID_USUARIO).append(EOL_).append(TAB);
		qryBuilder.append(TRIM).append(PARENTESIS_INIT).append(USUARIO);
		qryBuilder.append(PARENTESIS_FIN).append(AS).append(USUARIO).append(EOL_).append(TAB);
		qryBuilder.append(TRIM).append(PARENTESIS_INIT).append(PASSWORD);
		qryBuilder.append(PARENTESIS_FIN).append(AS).append(PASSWORD).append(EOL_).append(TAB);
		qryBuilder.append(EstablecimientoSql.ID_ESTABLECIMIENTO).append(EOL);
		
		qryBuilder.append(FROM).append(EOL).append(TAB);
		qryBuilder.append(TABLE_NAME).append(EOL);
		
		qryBuilder.append(WHERE).append(EOL).append(TAB);
		qryBuilder.append(EstablecimientoSql.ID_ESTABLECIMIENTO).append(SET_PARAM);
		
		
		GET_BY_ESTABLECIMIENTO = qryBuilder.toString();
		clearAndReuseStringBuilder(qryBuilder);
	}
}