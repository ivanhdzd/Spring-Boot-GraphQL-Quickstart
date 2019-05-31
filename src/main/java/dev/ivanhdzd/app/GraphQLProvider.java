package dev.ivanhdzd.app;

import java.io.IOException;
import java.net.URL;

import javax.annotation.PostConstruct;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component
public class GraphQLProvider {
	private GraphQL _graphQL;

	@Bean
	public GraphQL graphQL() {
		return _graphQL;
	}

	@Autowired
	public GraphQLDataFetchers graphQLDataFetchers;

	@PostConstruct
	public void init() throws IOException {
		URL url = Resources.getResource("schema.graphql");
		String sdl = Resources.toString(url, Charsets.UTF_8);
		GraphQLSchema graphQLSchema = _buildSchema(sdl);
		this._graphQL = GraphQL.newGraphQL(graphQLSchema).build();
	}

	private GraphQLSchema _buildSchema(String sdl) {
		TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
		RuntimeWiring runtimeWiring = _buildHiring();
		SchemaGenerator schemaGenerator = new SchemaGenerator();
		return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
	}

	private RuntimeWiring _buildHiring() {
		return RuntimeWiring.newRuntimeWiring()
			.type(newTypeWiring("Query")
				.dataFetcher("bookById", graphQLDataFetchers.getBookByIdDataFetcher()))
			.type(newTypeWiring("Book")
				.dataFetcher("author", graphQLDataFetchers.getAuthorDataFetcher()))
			.build();
	}
}