package io.gumga.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gumga.core.QueryObjectElement;
import io.gumga.core.utils.ReflectionUtils;
import io.gumga.domain.AbstractStringCriterionParser;
import io.gumga.domain.GumgaQueryParserProvider;
import io.gumga.domain.domains.GumgaMoney;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.Normalizer;
import java.util.*;
import org.apache.commons.lang.StringEscapeUtils;

public class GumgaGenericRepositoryHelper {

    private static Map<GumgaHqlEntry, GumgaHqlElement> hqlConverter;

    private GumgaGenericRepositoryHelper() {
    }

    /**
     * Faz substituição dos operadores especificados no atributo hqlConverter
     * para operadores do tipo SQL
     *
     * @return operadores sql
     */
    public static Map<GumgaHqlEntry, GumgaHqlElement> getHqlConverter() {
        if (hqlConverter == null) {
            hqlConverter = new HashMap<>();

            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.DEFAULT, "eq"), new GumgaHqlElement("='", "'"));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.DEFAULT, "ne"), new GumgaHqlElement("!='", "'"));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.DEFAULT, "ge"), new GumgaHqlElement(">='", "'"));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.DEFAULT, "le"), new GumgaHqlElement("<='", "'"));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.DEFAULT, "gt"), new GumgaHqlElement(">'", "'"));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.DEFAULT, "lt"), new GumgaHqlElement("<'", "'"));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.DEFAULT, "contains"), new GumgaHqlElement(" like '%", "%'"));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.DEFAULT, "not_contains"), new GumgaHqlElement(" not like '%", "%'"));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.DEFAULT, "starts_with"), new GumgaHqlElement(" like '", "%'"));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.DEFAULT, "ends_with"), new GumgaHqlElement(" like '%", "'"));

            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.TEXT, "contains"), new GumgaHqlElement(" like '%", "%'"));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.TEXT, "not_contains"), new GumgaHqlElement(" not like '%", "%'"));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.TEXT, "starts_with"), new GumgaHqlElement(" like '", "%'"));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.TEXT, "ends_with"), new GumgaHqlElement(" like '%", "'"));

            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.NUMBER, "eq"), new GumgaHqlElement("=", ""));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.NUMBER, "ne"), new GumgaHqlElement("!=", ""));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.NUMBER, "ge"), new GumgaHqlElement(">=", ""));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.NUMBER, "le"), new GumgaHqlElement("<=", ""));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.NUMBER, "gt"), new GumgaHqlElement(">", ""));
            hqlConverter.put(new GumgaHqlEntry(GumgaFieldStereotype.NUMBER, "lt"), new GumgaHqlElement("<", ""));

        }
        return hqlConverter;
    }

    public static String getHql(QueryObjectElement qoe) {
        if ("string".equals(qoe.getAttributeType())) {
            GumgaHqlElement ghe = getHqlConverter().get(new GumgaHqlEntry(GumgaFieldStereotype.TEXT, qoe.getHql().trim()));
            return "obj." + qoe.getAttribute() + ghe.before + qoe.getValue() + ghe.after;
        }
        GumgaHqlElement ghe = getHqlConverter().get(new GumgaHqlEntry(GumgaFieldStereotype.DEFAULT, qoe.getHql().trim()));
        return "obj." + qoe.getAttribute() + ghe.before + qoe.getValue() + ghe.after;
    }

    public static GumgaFieldStereotype getFieldStereotype(Class type) {
        if (java.lang.String.class.equals(type)) {
            return GumgaFieldStereotype.TEXT;
        }
        if (java.lang.Byte.class.equals(type)) {
            return GumgaFieldStereotype.NUMBER;
        }
        if (java.lang.Double.class.equals(type)) {
            return GumgaFieldStereotype.NUMBER;
        }
        if (java.lang.Float.class.equals(type)) {
            return GumgaFieldStereotype.NUMBER;
        }
        if (java.lang.Integer.class.equals(type)) {
            return GumgaFieldStereotype.NUMBER;
        }
        if (java.lang.Long.class.equals(type)) {
            return GumgaFieldStereotype.NUMBER;
        }
        if (java.lang.Number.class.equals(type)) {
            return GumgaFieldStereotype.NUMBER;
        }
        if (java.lang.Short.class.equals(type)) {
            return GumgaFieldStereotype.NUMBER;
        }
        if (GumgaMoney.class.equals(type)) {
            return GumgaFieldStereotype.NUMBER;
        }

        return GumgaFieldStereotype.DEFAULT;
    }

    /**
     * Gera um Objeto QueryObjectElement através de uma String.
     *
     * @param s
     * @return lista de QueryObjectElement
     */
    public static List<QueryObjectElement> qoeFromString(String s) {
        List<QueryObjectElement> aRetornar = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode mainNode = mapper.readTree(s);
            Iterator<JsonNode> elements = mainNode.elements();
            while (elements.hasNext()) {
                QueryObjectElement qoe = new QueryObjectElement();
                aRetornar.add(qoe);
                JsonNode node = elements.next();
                System.out.println("---->" + node);
                if (node.has("attribute")) {
                    qoe.setAttribute(node.get("attribute").get("field").asText());
                    qoe.setAttributeType(node.get("attribute").get("type").asText());
                }
                if (node.has("condition")) {
                    qoe.setHql(node.get("condition").get("hql").asText());
                }
                qoe.setValue(node.get("value").asText());
            }
        } catch (IOException ex) {
            throw new GumgaGenericRepostoryHelperException(ex);
        }
        System.out.println();
        return aRetornar;

    }

    /**
     * Gera um HQL através das informações da entidade e do QueryObject
     * especificado por parâmetro.
     *
     * @param entityInformation
     * @param qoes
     * @return hql
     */
    public static String hqlFromQoes(JpaEntityInformation entityInformation, List<QueryObjectElement> qoes) {
        String aRetornar = "";

        for (QueryObjectElement qoe : qoes) {
            if ("NO_ATTRIBUTE".equals(qoe.getAttribute())) {
                aRetornar += " " + qoe.getValue() + " ";
            } else {
                Class type = String.class;
                Field field = ReflectionUtils.findField(entityInformation.getJavaType(), qoe.getAttribute());
                if (field != null) {
                    type = field.getType();
                }
                GumgaFieldStereotype fieldStereotype = getFieldStereotype(type);
                if (GumgaQueryParserProvider.defaultMap.equals(GumgaQueryParserProvider.getOracleLikeMap()) && fieldStereotype == GumgaFieldStereotype.TEXT) {
                    aRetornar += "upper(translate(obj." + qoe.getAttribute()
                            + ",'" + AbstractStringCriterionParser.SOURCE_CHARS + "','" + AbstractStringCriterionParser.TARGET_CHARS + "'"
                            + ")" + ")";
                } else {
                    aRetornar += "obj." + qoe.getAttribute();
                }

                GumgaHqlEntry het = new GumgaHqlEntry(fieldStereotype, qoe.getHql());
                GumgaHqlElement hel = getHqlConverter().get(het);
                aRetornar += hel.before + removeAcentos(qoe.getValue()).toUpperCase() + hel.after;
            }
        }
        return aRetornar;
    }

    /**
     * Remove acentos do parametro especificado
     *
     * @param str valor
     * @return parametro sem acentos
     */
    public static String removeAcentos(String str) {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = str.replaceAll("[^\\p{ASCII}]", "");
        return str;

    }
    
    public static String hql(String aqo){
        aqo = StringEscapeUtils.unescapeJavaScript(aqo);
        aqo = aqo.substring(1, aqo.length() - 1);
        List<QueryObjectElement> qoeFromString = qoeFromString(aqo);
        String hql="";
        for (QueryObjectElement e : qoeFromString) {
            hql+=e.isLogical()?e.toString():getHql(e);
        }
        return hql;
    }
    
    public static String hql(List<QueryObjectElement> qoes){
        String hql="";
        for (QueryObjectElement e : qoes) {
            hql+=e.isLogical()?e.toString():getHql(e);
        }
        return hql;
        
    }

    public static void main(String args[]) {

        String aqo = "\"[{\\\"attribute\\\":{\\\"field\\\":\\\"name\\\",\\\"type\\\":\\\"string\\\",\\\"label\\\":\\\"Nome\\\"},\\\"condition\\\":{\\\"hql\\\":\\\" contains \\\",\\\"label\\\":\\\" contém \\\",\\\"before\\\":\\\" like '%\\\",\\\"after\\\":\\\"%'\\\"},\\\"value\\\":\\\"Gum\\\"},{\\\"value\\\":\\\"AND\\\"},{\\\"attribute\\\":{\\\"field\\\":\\\"id\\\",\\\"type\\\":\\\"number\\\",\\\"label\\\":\\\"Id\\\"},\\\"condition\\\":{\\\"hql\\\":\\\" gt \\\",\\\"label\\\":\\\" maior que \\\",\\\"before\\\":\\\" >   \\\",\\\"after\\\":\\\"\\\"},\\\"value\\\":2}]\"";
        
        System.out.println (hql(aqo));
/*        
        aqo = StringEscapeUtils.unescapeJavaScript(aqo);
        aqo = aqo.substring(1, aqo.length() - 1);
        System.out.println("--->" + aqo + "\n");
        List<QueryObjectElement> qoeFromString = qoeFromString(aqo);
        String hql="";
        for (QueryObjectElement e : qoeFromString) {
            System.out.println("--->" + (e.isLogical()?e.toString():getHql(e)));
        }
        System.out.println("\n--->>" + qoeFromString);*/
    }

}
