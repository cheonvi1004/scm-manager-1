//@flow
import React from "react";
import { configure, shallow } from "enzyme";
import Adapter from "enzyme-adapter-react-16";
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";

import {
  FETCH_USERS,
  FETCH_USERS_SUCCESS,
  fetchUsers,
  FETCH_USERS_FAILURE
} from "./users";

import reducer from "./users";

import "raf/polyfill";

configure({ adapter: new Adapter() });

const userZaphod = {
  active: true,
  admin: true,
  creationDate: "2018-07-11T12:23:49.027Z",
  displayName: "Z. Beeblebrox",
  mail: "president@heartofgold.universe",
  name: "zaphod",
  password: "__dummypassword__",
  type: "xml",
  properties: {},
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/rest/v2/users/zaphod"
    },
    delete: {
      href: "http://localhost:8081/scm/api/rest/v2/users/zaphod"
    },
    update: {
      href: "http://localhost:8081/scm/api/rest/v2/users/zaphod"
    }
  }
};

const userFord = {
  active: true,
  admin: false,
  creationDate: "2018-07-06T13:21:18.459Z",
  displayName: "F. Prefect",
  mail: "ford@prefect.universe",
  name: "ford",
  password: "__dummypassword__",
  type: "xml",
  properties: {},
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/rest/v2/users/ford"
    },
    delete: {
      href: "http://localhost:8081/scm/api/rest/v2/users/ford"
    },
    update: {
      href: "http://localhost:8081/scm/api/rest/v2/users/ford"
    }
  }
};

const responseBodyZaphod = {
  page: 0,
  pageTotal: 1,
  _links: {
    self: {
      href: "http://localhost:3000/scm/api/rest/v2/users/?page=0&pageSize=10"
    },
    first: {
      href: "http://localhost:3000/scm/api/rest/v2/users/?page=0&pageSize=10"
    },
    last: {
      href: "http://localhost:3000/scm/api/rest/v2/users/?page=0&pageSize=10"
    },
    create: {
      href: "http://localhost:3000/scm/api/rest/v2/users/"
    }
  },
  _embedded: {
    users: [userZaphod]
  }
};

const responseBody = {
  page: 0,
  pageTotal: 1,
  _links: {
    self: {
      href: "http://localhost:3000/scm/api/rest/v2/users/?page=0&pageSize=10"
    },
    first: {
      href: "http://localhost:3000/scm/api/rest/v2/users/?page=0&pageSize=10"
    },
    last: {
      href: "http://localhost:3000/scm/api/rest/v2/users/?page=0&pageSize=10"
    },
    create: {
      href: "http://localhost:3000/scm/api/rest/v2/users/"
    }
  },
  _embedded: {
    users: [userZaphod, userFord]
  }
};

const response = {
  headers: { "content-type": "application/json" },
  responseBody
};

describe("fetch tests", () => {
  const mockStore = configureMockStore([thunk]);
  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  test("successful users fetch", () => {
    fetchMock.getOnce("/scm/api/rest/v2/users", response);

    const expectedActions = [
      { type: FETCH_USERS },
      {
        type: FETCH_USERS_SUCCESS,
        payload: response
      }
    ];

    const store = mockStore({});

    return store.dispatch(fetchUsers()).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  test("me fetch failed", () => {
    fetchMock.getOnce("/scm/api/rest/v2/users", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchUsers()).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_USERS);
      expect(actions[1].type).toEqual(FETCH_USERS_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });
});

describe("reducer tests", () => {
  test("users request", () => {
    var newState = reducer({}, { type: FETCH_USERS });
    expect(newState.users.loading).toBeTruthy();
    expect(newState.users.error).toBeNull();
  });

  test("fetch users successful", () => {
    var newState = reducer(
      {},
      { type: FETCH_USERS_SUCCESS, payload: responseBody }
    );

    expect(newState.users).toEqual({
      entries: ["zaphod", "ford"],
      error: null,
      loading: false
    });

    expect(newState.usersByNames).toEqual({
      zaphod: {
        entry: userZaphod
      },
      ford: {
        entry: userFord
      }
    });
  });

  test("reducer does not replace whole usersByNames map", () => {
    const oldState = {
      usersByNames: {
        ford: {
          entry: userFord
        }
      }
    };

    const newState = reducer(oldState, {
      type: FETCH_USERS_SUCCESS,
      payload: responseBodyZaphod
    });
    expect(newState.usersByNames["zaphod"]).toBeDefined();
    expect(newState.usersByNames["ford"]).toBeDefined();
  });
});
