import React from 'react';
import keycloak from './Keycloak';
import { BrowserRouter, Routes, Route } from "react-router-dom"; 
import ItemPage from './pages/ItemPage';
import './App.css';

function App() {
  const [state, setState] = React.useState({ keycloak: keycloak, authenticated: false });

  React.useMemo(() => {
    if(keycloak.authenticated === null || !keycloak.authenticated) {
      keycloak.init({
        onLoad: 'check-sso',
        checkLoginIframe: false,
        promiseType: 'native'
      }).then((authenticated) => {
        if (authenticated) {
          localStorage.setItem('token', keycloak.token);
          setState({ keycloak: keycloak, authenticated: authenticated })
        }
      }).catch(console.error);
    }
  }, []);


  return (
    <React.StrictMode>
      {state.keycloak && state.authenticated &&
      <BrowserRouter basename="/magellan">
        <Routes>
          <Route path="/">
            <Route index element={<ItemPage objectType = {"brand"} />} />
            <Route path="brands" element={<ItemPage objectType = {"brand"} />} />
            <Route path="masterdomains" element={<ItemPage objectType = {"masterdomain"} />} />
            <Route path="ipranges" element={<ItemPage objectType = {"iprange"} />} />
            <Route path="domains" element={<ItemPage objectType = {"domain"} />} />
            <Route path="ips" element={<ItemPage objectType = {"ip"} />} />
          </Route>
        </Routes>     
      </BrowserRouter>
      }
    </React.StrictMode>
  );
}

export default App;
