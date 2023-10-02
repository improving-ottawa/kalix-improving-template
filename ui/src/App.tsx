import React from 'react';
import './App.css';
import {BrowserRouter, Route, Routes} from "react-router-dom";
import Layout from "./Layout";
import Home from "./screens/Home";
import ChangeName from "./screens/ChangeName";
import {Provider} from "react-redux";
import {store} from "./redux/store";

function App() {
  return (
      <Provider store={store}>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<Layout />}>
              <Route index element={<Home />} />
              <Route path="change-name" element={<ChangeName />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </Provider>
  );
}

export default App;
