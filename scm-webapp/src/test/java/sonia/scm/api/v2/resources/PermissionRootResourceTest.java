package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableList;
import com.google.inject.util.Providers;
import de.otto.edison.hal.HalRepresentation;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.assertj.core.util.Lists;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.VndMediaType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static sonia.scm.api.v2.resources.DispatcherMock.createDispatcher;
import static sonia.scm.api.v2.resources.RepositoryPermissionDto.GROUP_PREFIX;

@Slf4j
@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class PermissionRootResourceTest extends RepositoryTestBase {
  private static final String REPOSITORY_NAMESPACE = "repo_namespace";
  private static final String REPOSITORY_NAME = "repo";
  private static final String PERMISSION_WRITE = "repository:permissionWrite:" + REPOSITORY_NAME;
  private static final String PERMISSION_READ = "repository:permissionRead:" + REPOSITORY_NAME;
  private static final String PERMISSION_OWNER = "repository:modify:" + REPOSITORY_NAME;

  private static final String PERMISSION_NAME = "perm";
  private static final String PATH_OF_ALL_PERMISSIONS = REPOSITORY_NAMESPACE + "/" + REPOSITORY_NAME + "/permissions/";
  private static final String PATH_OF_ONE_PERMISSION = PATH_OF_ALL_PERMISSIONS + PERMISSION_NAME;
  private static final String PERMISSION_TEST_PAYLOAD = "{ \"name\" : \"permission_name\", \"type\" : \"READ\"  }";
  private static final ArrayList<RepositoryPermission> TEST_PERMISSIONS = Lists
    .newArrayList(
      new RepositoryPermission("user_write", PermissionType.WRITE, false),
      new RepositoryPermission("user_read", PermissionType.READ, false),
      new RepositoryPermission("user_owner", PermissionType.OWNER, false),
      new RepositoryPermission("group_read", PermissionType.READ, true),
      new RepositoryPermission("group_write", PermissionType.WRITE, true),
      new RepositoryPermission("group_owner", PermissionType.OWNER, true)
    );
  private final ExpectedRequest requestGETAllPermissions = new ExpectedRequest()
    .description("GET all permissions")
    .method("GET")
    .path(PATH_OF_ALL_PERMISSIONS);
  private final ExpectedRequest requestPOSTPermission = new ExpectedRequest()
    .description("create new permission")
    .method("POST")
    .content(PERMISSION_TEST_PAYLOAD)
    .path(PATH_OF_ALL_PERMISSIONS);
  private final ExpectedRequest requestGETPermission = new ExpectedRequest()
    .description("GET permission")
    .method("GET")
    .path(PATH_OF_ONE_PERMISSION);
  private final ExpectedRequest requestDELETEPermission = new ExpectedRequest()
    .description("delete permission")
    .method("DELETE")
    .path(PATH_OF_ONE_PERMISSION);
  private final ExpectedRequest requestPUTPermission = new ExpectedRequest()
    .description("update permission")
    .method("PUT")
    .content(PERMISSION_TEST_PAYLOAD)
    .path(PATH_OF_ONE_PERMISSION);

  private Dispatcher dispatcher;

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  private RepositoryManager repositoryManager;

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private RepositoryPermissionToRepositoryPermissionDtoMapperImpl permissionToPermissionDtoMapper;

  @InjectMocks
  private PermissionDtoToPermissionMapperImpl permissionDtoToPermissionMapper;

  private RepositoryPermissionCollectionToDtoMapper repositoryPermissionCollectionToDtoMapper;

  private PermissionRootResource permissionRootResource;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  @BeforeEach
  @Before
  public void prepareEnvironment() {
    initMocks(this);
    repositoryPermissionCollectionToDtoMapper = new RepositoryPermissionCollectionToDtoMapper(permissionToPermissionDtoMapper, resourceLinks);
    permissionRootResource = new PermissionRootResource(permissionDtoToPermissionMapper, permissionToPermissionDtoMapper, repositoryPermissionCollectionToDtoMapper, resourceLinks, repositoryManager);
    super.permissionRootResource = Providers.of(permissionRootResource);
    dispatcher = createDispatcher(getRepositoryRootResource());
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @After
  public void unbind() {
    ThreadContext.unbindSubject();
  }

  @TestFactory
  @DisplayName("test endpoints on missing repository")
  Stream<DynamicTest> missedRepositoryTestFactory() {
    return createDynamicTestsToAssertResponses(
      requestGETAllPermissions.expectedResponseStatus(404),
      requestGETPermission.expectedResponseStatus(404),
      requestPOSTPermission.expectedResponseStatus(404),
      requestDELETEPermission.expectedResponseStatus(404),
      requestPUTPermission.expectedResponseStatus(404));
  }

  @TestFactory
  @DisplayName("test endpoints on missing permissions and user is Admin")
  Stream<DynamicTest> missedPermissionTestFactory() {
    Repository mockRepository = new Repository(REPOSITORY_NAME, "git", REPOSITORY_NAMESPACE, REPOSITORY_NAME);
    when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(mockRepository);
    return createDynamicTestsToAssertResponses(
      requestGETPermission.expectedResponseStatus(404),
      requestPOSTPermission.expectedResponseStatus(201),
      requestGETAllPermissions.expectedResponseStatus(200),
      requestDELETEPermission.expectedResponseStatus(204),
      requestPUTPermission.expectedResponseStatus(404));
  }

  @TestFactory
  @DisplayName("test endpoints on missing permissions and user is not Admin")
  Stream<DynamicTest> missedPermissionUserForbiddenTestFactory() {
    doThrow(AuthorizationException.class).when(repositoryManager).get(any(NamespaceAndName.class));
    return createDynamicTestsToAssertResponses(
      requestGETPermission.expectedResponseStatus(403),
      requestPOSTPermission.expectedResponseStatus(403),
      requestGETAllPermissions.expectedResponseStatus(403),
      requestDELETEPermission.expectedResponseStatus(403),
      requestPUTPermission.expectedResponseStatus(403));
  }

  @Test
  public void userWithPermissionWritePermissionShouldGetAllPermissionsWithCreateAndUpdateLinks() throws URISyntaxException {
    createUserWithRepositoryAndPermissions(TEST_PERMISSIONS, PERMISSION_WRITE);
    assertGettingExpectedPermissions(ImmutableList.copyOf(TEST_PERMISSIONS), PERMISSION_WRITE);
  }

  @Test
  public void userWithPermissionReadPermissionShouldGetAllPermissionsWithoutCreateAndUpdateLinks() throws URISyntaxException {
    createUserWithRepositoryAndPermissions(TEST_PERMISSIONS, PERMISSION_READ);
    assertGettingExpectedPermissions(ImmutableList.copyOf(TEST_PERMISSIONS), PERMISSION_READ);
  }

  @Test
  public void shouldGetAllPermissions() throws URISyntaxException {
    createUserWithRepositoryAndPermissions(TEST_PERMISSIONS, PERMISSION_READ);
    assertGettingExpectedPermissions(ImmutableList.copyOf(TEST_PERMISSIONS), PERMISSION_READ);
  }

  @Test
  public void shouldGetPermissionByName() throws URISyntaxException {
    createUserWithRepositoryAndPermissions(TEST_PERMISSIONS, PERMISSION_READ);
    RepositoryPermission expectedPermission = TEST_PERMISSIONS.get(0);
    assertExpectedRequest(requestGETPermission
      .expectedResponseStatus(200)
      .path(PATH_OF_ALL_PERMISSIONS + expectedPermission.getName())
      .responseValidator((response) -> {
        String body = response.getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        try {
          RepositoryPermissionDto actualRepositoryPermissionDto = mapper.readValue(body, RepositoryPermissionDto.class);
          assertThat(actualRepositoryPermissionDto)
            .as("response payload match permission object model")
            .isEqualToComparingFieldByFieldRecursively(getExpectedPermissionDto(expectedPermission, PERMISSION_READ))
          ;
        } catch (IOException e) {
          fail();
        }
      })
    );
  }


  @Test
  public void shouldGet400OnCreatingNewPermissionWithNotAllowedCharacters() throws URISyntaxException {
    // the @ character at the begin of the name is not allowed
    createUserWithRepository("user");
    String permissionJson = "{ \"name\": \"@permission\", \"type\": \"OWNER\" }";
    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + PATH_OF_ALL_PERMISSIONS)
      .content(permissionJson.getBytes())
      .contentType(VndMediaType.PERMISSION);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());

    // the whitespace at the begin opf the name is not allowed
    permissionJson = "{ \"name\": \" permission\", \"type\": \"OWNER\" }";
    request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + PATH_OF_ALL_PERMISSIONS)
      .content(permissionJson.getBytes())
      .contentType(VndMediaType.PERMISSION);
    response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
  }

  @Test
  public void shouldGetCreatedPermissions() throws URISyntaxException {
    createUserWithRepositoryAndPermissions(TEST_PERMISSIONS, PERMISSION_WRITE);
    RepositoryPermission newPermission = new RepositoryPermission("new_group_perm", PermissionType.WRITE, true);
    ArrayList<RepositoryPermission> permissions = Lists.newArrayList(TEST_PERMISSIONS);
    permissions.add(newPermission);
    ImmutableList<RepositoryPermission> expectedPermissions = ImmutableList.copyOf(permissions);
    assertExpectedRequest(requestPOSTPermission
      .content("{\"name\" : \"" + newPermission.getName() + "\" , \"type\" : \"WRITE\" , \"groupPermission\" : true}")
      .expectedResponseStatus(201)
      .responseValidator(response -> assertThat(response.getContentAsString())
        .as("POST response has no body")
        .isBlank())
    );
    assertGettingExpectedPermissions(expectedPermissions, PERMISSION_WRITE);
  }

  @Test
  public void shouldNotAddExistingPermission() throws URISyntaxException {
    createUserWithRepositoryAndPermissions(TEST_PERMISSIONS, PERMISSION_WRITE);
    RepositoryPermission newPermission = TEST_PERMISSIONS.get(0);
    assertExpectedRequest(requestPOSTPermission
      .content("{\"name\" : \"" + newPermission.getName() + "\" , \"type\" : \"WRITE\" , \"groupPermission\" : false}")
      .expectedResponseStatus(409)
    );
  }

  @Test
  public void shouldGetUpdatedPermissions() throws URISyntaxException {
    createUserWithRepositoryAndPermissions(TEST_PERMISSIONS, PERMISSION_WRITE);
    RepositoryPermission modifiedPermission = TEST_PERMISSIONS.get(0);
    // modify the type to owner
    modifiedPermission.setType(PermissionType.OWNER);
    ImmutableList<RepositoryPermission> expectedPermissions = ImmutableList.copyOf(TEST_PERMISSIONS);
    assertExpectedRequest(requestPUTPermission
      .content("{\"name\" : \"" + modifiedPermission.getName() + "\" , \"type\" : \"OWNER\" , \"groupPermission\" : false}")
      .path(PATH_OF_ALL_PERMISSIONS + modifiedPermission.getName())
      .expectedResponseStatus(204)
      .responseValidator(response -> assertThat(response.getContentAsString())
        .as("PUT response has no body")
        .isBlank())
    );
    assertGettingExpectedPermissions(expectedPermissions, PERMISSION_WRITE);
  }


  @Test
  public void shouldDeletePermissions() throws URISyntaxException {
    createUserWithRepositoryAndPermissions(TEST_PERMISSIONS, PERMISSION_OWNER);
    RepositoryPermission deletedPermission = TEST_PERMISSIONS.get(0);
    ImmutableList<RepositoryPermission> expectedPermissions = ImmutableList.copyOf(TEST_PERMISSIONS.subList(1, TEST_PERMISSIONS.size()));
    assertExpectedRequest(requestDELETEPermission
      .path(PATH_OF_ALL_PERMISSIONS + deletedPermission.getName())
      .expectedResponseStatus(204)
      .responseValidator(response -> assertThat(response.getContentAsString())
        .as("DELETE response has no body")
        .isBlank())
    );
    assertGettingExpectedPermissions(expectedPermissions, PERMISSION_READ);
  }

  @Test
  public void deletingNotExistingPermissionShouldProcess() throws URISyntaxException {
    createUserWithRepositoryAndPermissions(TEST_PERMISSIONS, PERMISSION_OWNER);
    RepositoryPermission deletedPermission = TEST_PERMISSIONS.get(0);
    ImmutableList<RepositoryPermission> expectedPermissions = ImmutableList.copyOf(TEST_PERMISSIONS.subList(1, TEST_PERMISSIONS.size()));
    assertExpectedRequest(requestDELETEPermission
      .path(PATH_OF_ALL_PERMISSIONS + deletedPermission.getName())
      .expectedResponseStatus(204)
      .responseValidator(response -> assertThat(response.getContentAsString())
        .as("DELETE response has no body")
        .isBlank())
    );
    assertGettingExpectedPermissions(expectedPermissions, PERMISSION_READ);
    assertExpectedRequest(requestDELETEPermission
      .path(PATH_OF_ALL_PERMISSIONS + deletedPermission.getName())
      .expectedResponseStatus(204)
      .responseValidator(response -> assertThat(response.getContentAsString())
        .as("DELETE response has no body")
        .isBlank())
    );
    assertGettingExpectedPermissions(expectedPermissions, PERMISSION_READ);
  }

  private void assertGettingExpectedPermissions(ImmutableList<RepositoryPermission> expectedPermissions, String userPermission) throws URISyntaxException {
    assertExpectedRequest(requestGETAllPermissions
      .expectedResponseStatus(200)
      .responseValidator((response) -> {
        String body = response.getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        try {
          HalRepresentation halRepresentation = mapper.readValue(body, HalRepresentation.class);
          List<HalRepresentation> actualPermissionDtos = halRepresentation.getEmbedded().getItemsBy("permissions", HalRepresentation.class);
          List<RepositoryPermissionDto> repositoryPermissionDtoStream = actualPermissionDtos.stream()
            .map(hal -> {
              RepositoryPermissionDto result = new RepositoryPermissionDto();
              result.setName(hal.getAttribute("name").asText());
              result.setType(hal.getAttribute("type").asText());
              result.setGroupPermission(hal.getAttribute("groupPermission").asBoolean());
              result.add(hal.getLinks());
              return result;
            }).collect(Collectors.toList());
          assertThat(repositoryPermissionDtoStream)
            .as("response payload match permission object models")
            .hasSize(expectedPermissions.size())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(getExpectedPermissionDtos(Lists.newArrayList(expectedPermissions), userPermission))
          ;
        } catch (IOException e) {
          fail();
        }
      })
    );
  }

  private RepositoryPermissionDto[] getExpectedPermissionDtos(ArrayList<RepositoryPermission> permissions, String userPermission) {
    return permissions
      .stream()
      .map(p -> getExpectedPermissionDto(p, userPermission))
      .toArray(RepositoryPermissionDto[]::new);
  }

  private RepositoryPermissionDto getExpectedPermissionDto(RepositoryPermission permission, String userPermission) {
    RepositoryPermissionDto result = new RepositoryPermissionDto();
    result.setName(permission.getName());
    result.setGroupPermission(permission.isGroupPermission());
    result.setType(permission.getType().name());
    String permissionName = Optional.of(permission.getName())
      .filter(p -> !permission.isGroupPermission())
      .orElse(GROUP_PREFIX + permission.getName());
    String permissionHref = "/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + PATH_OF_ALL_PERMISSIONS + permissionName;
    if (PERMISSION_READ.equals(userPermission)) {
      result.add(linkingTo()
        .self(permissionHref)
        .build());
    } else {
      result.add(linkingTo()
        .self(permissionHref)
        .single(link("update", permissionHref))
        .single(link("delete", permissionHref))
        .build());
    }
    return result;
  }

  private Repository createUserWithRepository(String userPermission) {
    Repository mockRepository = new Repository();
    mockRepository.setId(REPOSITORY_NAME);
    mockRepository.setNamespace(REPOSITORY_NAMESPACE);
    mockRepository.setName(REPOSITORY_NAME);
    when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(mockRepository);
    when(subject.isPermitted(userPermission != null ? eq(userPermission) : any(String.class))).thenReturn(true);
    return mockRepository;
  }

  private void createUserWithRepositoryAndPermissions(ArrayList<RepositoryPermission> permissions, String userPermission) {
    createUserWithRepository(userPermission).setPermissions(permissions);
  }

  private Stream<DynamicTest> createDynamicTestsToAssertResponses(ExpectedRequest... expectedRequests) {
    return Stream.of(expectedRequests)
      .map(entry -> dynamicTest("the endpoint " + entry.description + " should return the status code " + entry.expectedResponseStatus, () -> assertExpectedRequest(entry)));
  }

  private void assertExpectedRequest(ExpectedRequest entry) throws URISyntaxException {
    MockHttpResponse response = new MockHttpResponse();
    HttpRequest request = MockHttpRequest
      .create(entry.method, "/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + entry.path)
      .content(entry.content)
      .contentType(VndMediaType.PERMISSION);
    dispatcher.invoke(request, response);
    log.info("Test the Request :{}", entry);
    assertThat(response.getStatus())
      .as("assert status code")
      .isEqualTo(entry.expectedResponseStatus);
    if (entry.responseValidator != null) {
      entry.responseValidator.accept(response);
    }
  }

  @ToString
  public class ExpectedRequest {
    private String description;
    private String method;
    private String path;
    private int expectedResponseStatus;
    private byte[] content = new byte[]{};
    private Consumer<MockHttpResponse> responseValidator;

    public ExpectedRequest description(String description) {
      this.description = description;
      return this;
    }

    public ExpectedRequest method(String method) {
      this.method = method;
      return this;
    }

    public ExpectedRequest path(String path) {
      this.path = path;
      return this;
    }

    public ExpectedRequest content(String content) {
      if (content != null) {
        this.content = content.getBytes();
      }
      return this;
    }

    ExpectedRequest expectedResponseStatus(int expectedResponseStatus) {
      this.expectedResponseStatus = expectedResponseStatus;
      return this;
    }

    ExpectedRequest responseValidator(Consumer<MockHttpResponse> responseValidator) {
      this.responseValidator = responseValidator;
      return this;
    }
  }

}
