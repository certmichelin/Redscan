import React from 'react';
import { Alert, Container, Row, Col, Button, ButtonGroup } from "react-bootstrap";
import NumberEntriesSelection from '../components/NumberEntriesSelection';
import SearchBar from '../components/SearchBar';
import AddScannableModal from "../components/AddScannableModal";
import AddNonScannableModal from "../components/AddNonScannableModal";
import ImportScannableModal from "../components/ImportScannableModal";
import ImportNonScannableModal from "../components/ImportNonScannableModal";
import ScannableTable from "../components/ScannableTable";
import NonScannableTable from "../components/NonScannableTable";
import PaginationTable from "../components/PaginationTable";
import keycloak from "../Keycloak";

function ItemPage({ objectType }) {
    const [showAdd, setShowAdd] = React.useState(false);
    const handleCloseAdd = () => setShowAdd(false);
    const handleShowAdd = () => setShowAdd(true);

    const [showImport, setShowImport] = React.useState(false);
    const handleCloseImport = () => setShowImport(false);
    const handleShowImport = () => setShowImport(true);

    const [totalNumberItems, setTotalNumberItems] = React.useState(1);
    const [items, setItems] = React.useState([]);
    const [numberEntries, setNumberEntries] = React.useState("10");
    const [numberPages, setNumberPages] = React.useState("1");
    const [currentPage, setCurrentPage] = React.useState(1);

    const [sortField, setSortField] = React.useState("_id");
    const [order, setOrder] = React.useState("asc");

    const scannableItems = ["brand", "masterdomain", "iprange"];

    const [filterValue, setFilterValue] = React.useState(1);

    const hasPageBeenRendered = React.useRef({effectFilter : false, 
                                              effectTotalNumberItems : false, 
                                              effectNumberEntries : false,
                                              effectCurrentPage : false, 
                                              effectSortField : false, 
                                              effectOrder : false,
                                              effectSearch : false
                                              });
    

    // Only used in masterdomains and non-scannable items
    var filters = [];
    if (scannableItems.indexOf(objectType) > -1) { // Case of masterdomain
      filters = [
        { name: 'All', value: 1, apiEndpoint: "" },
        { name: 'To review', value: 2, apiEndpoint: "toReview" },
        { name: 'In scope', value: 3, apiEndpoint: "inScope" },
        { name: 'Out of scope', value: 4, apiEndpoint: "outOfScope" },
      ];
    }
    else { // Case of non-scannable items
      filters = [
        { name: 'All', value: 1, apiEndpoint: "" },
        { name: 'Blocked', value: 2, apiEndpoint: "blocked" },
        { name: 'Not blocked', value: 3, apiEndpoint: "nonBlocked" }
      ];
    }
    
    function getFilterEndpoint() {
      const filter = filters.find(filter => filter.value === filterValue);
      return filter.apiEndpoint;
    }

    function updateToken() {
      keycloak.updateToken(120).then(() => {
        localStorage.setItem('token', keycloak.token);
      });
    }
    

    // API CALLS
    function getNumberItems(url) {
      setTimeout(() => {
        fetch(url, { headers: { 'Authorization': String('Bearer ' + localStorage.getItem('token')) } })
          .then(response => response.json())
          .then(data => {
            setTotalNumberItems(data.length);
            updateItems();
          });
      }, 1000);
    }

    function getItems(url) {
      fetch(url, { headers: { 'Authorization': String('Bearer ' + localStorage.getItem('token')) } })
        .then(response => {
          if (response.ok) {
            return response.json();
          }
          throw new Error('Something went wrong');
        })
        .then(data => setItems(data))
        .catch(() => {
          setErrorMessage("Retrieval of " + objectType + "s impossible.");
          setShowError(true);
        });
    }

    function handleBlock(item) {
      const delay = ms => new Promise(resolve => setTimeout(resolve, ms));
  
      if(keycloak.authenticated) {
        keycloak.updateToken(120).then(() => {
          localStorage.setItem('token', keycloak.token);
        })
  
        const requestOptions = {
          method: 'PUT',
          headers: { 'Authorization': String('Bearer ' + localStorage.getItem('token')) }
        };
  
        var actionWanted = "";
        (item.blocked) ? actionWanted = "unblock" : actionWanted = "block";
  
        fetch(String('https://' + process.env.REACT_APP_PUBLIC_DOMAIN + '/magellan-api/api/' + objectType + 's/' + actionWanted + '/' + item.id), requestOptions)
          .then(async response => {
            if (!response.ok) {
              setErrorMessage(actionWanted.charAt(0).toUpperCase() + actionWanted.slice(1) + "ing failed.");
              setShowError(true);
            }
            else { 
              setSuccessMessage(actionWanted.charAt(0).toUpperCase() + actionWanted.slice(1) + "ing succeeded.");
              setShowSuccess(true);
              await delay(1000);
              handleUpdate();
            }
          });
      }
    }


    // UPDATE TABLE ENTRIES
    function updateItems() {
      updateToken();
      var url = "";

      if (filterValue === 1) {
        if (search === "") {
          url = 'https://' + process.env.REACT_APP_PUBLIC_DOMAIN + '/magellan-api/api/' + objectType + 's/' + currentPage + '/' + numberEntries + '/sort?field=' + sortField + '&order=' + order;
        }
        else {
          url = 'https://' + process.env.REACT_APP_PUBLIC_DOMAIN + '/magellan-api/api/' + objectType + 's/search/' + search + '/' + currentPage + '/' + numberEntries + '/sort?field=' + sortField + '&order=' + order;
        }
      }
      else {
        var filterEndpoint = getFilterEndpoint();
        url = 'https://' + process.env.REACT_APP_PUBLIC_DOMAIN + '/magellan-api/api/' + objectType + 's/' + filterEndpoint + '/' + currentPage + '/' + numberEntries + '/sort?field=' + sortField + '&order=' + order;
      }

      getItems(url);
    }

    function handleUpdate() {
      if(keycloak.authenticated) {
        var url = "";

        if (filterValue === 1) {
          if (search === "") {
            url = 'https://' + process.env.REACT_APP_PUBLIC_DOMAIN + '/magellan-api/api/' + objectType + 's' + '/sort?field=' + sortField + '&order=' + order;
          }
          else {
            url = 'https://' + process.env.REACT_APP_PUBLIC_DOMAIN + '/magellan-api/api/' + objectType + 's/search/' + search + '/sort?field=' + sortField + '&order=' + order;
          }
        }
        else {
          var filterEndpoint = getFilterEndpoint();
          setSearch(""); // Reinit search
          url = 'https://' + process.env.REACT_APP_PUBLIC_DOMAIN + '/magellan-api/api/' + objectType + 's/' + filterEndpoint + '/sort?field=' + sortField + '&order=' + order;
        }

        getNumberItems(url);
      }
    }

    // Retrieve total number of items before updating entries to print
    React.useEffect(() => {
      updateItems();
    }, []) 

    React.useEffect(() => {
      if (hasPageBeenRendered.current["effectFilter"]) {
        handleUpdate();
      }
      hasPageBeenRendered.current["effectFilter"] = true;
    }, [filterValue])

    // Update number of pages
    React.useEffect(() => {
      if (hasPageBeenRendered.current["effectTotalNumberItems"] && hasPageBeenRendered.current["effectNumberEntries"]) {
        var numberPages = 1;
        if (totalNumberItems !== 0) numberPages = Math.ceil(totalNumberItems / numberEntries);
        setNumberPages(numberPages);
      }
      hasPageBeenRendered.current["effectTotalNumberItems"] = true;
      hasPageBeenRendered.current["effectNumberEntries"] = true;
    }, [totalNumberItems, numberEntries]) // On update of total number of items or number of entries to print

    // Change number of entries displayed
    React.useEffect(() => {
      if (hasPageBeenRendered.current["effectNumberEntries"] && 
          hasPageBeenRendered.current["effectCurrentPage"] &&
          hasPageBeenRendered.current["effectSortField"] &&
          hasPageBeenRendered.current["effectOrder"]) 
      {
        if(keycloak.authenticated) {
          updateItems();
        }
      }

      hasPageBeenRendered.current["effectNumberEntries"] = true;
      hasPageBeenRendered.current["effectCurrentPage"] = true;
      hasPageBeenRendered.current["effectSortField"] = true;
      hasPageBeenRendered.current["effectOrder"] = true;
    }, [numberEntries, currentPage, sortField, order]) // On update of number of entries to print or current page consulted

    
    // SEARCH
    const [search, setSearch] = React.useState("");

    function handleSearch(input) {
      setCurrentPage(1); // Reset pagination for each search
      setFilterValue(1); // Reinit filters if necessary
      setSearch(input);
    }

    React.useEffect(() => {
      if (hasPageBeenRendered.current["effectSearch"]){
        handleUpdate();
        console.log("state 6");
      }
      hasPageBeenRendered.current["effectSearch"] = true;
    }, [search])


    // ERROR AND SUCCESS MESSAGES
    const [errorMessage, setErrorMessage] = React.useState("");
    const [successMessage, setSuccessMessage] = React.useState("");
    const [showError, setShowError] = React.useState(false);
    const [showSuccess, setShowSuccess] = React.useState(false);

    React.useEffect(() => {
      if(showSuccess) window.setTimeout(()=>{ setShowSuccess(false); }, 4000);
      if(showError) window.setTimeout(()=>{ setShowError(false); }, 4000);
    }, [showSuccess, showError])


    // VENTILATION
    function handleVentilation() {
      updateToken();
      const requestOptions = {
        method: 'POST',
        headers: { 'Authorization': String('Bearer ' + localStorage.getItem('token')) },
        body: ""
      };

      fetch('https://' + process.env.REACT_APP_PUBLIC_DOMAIN + '/magellan-api/api/' + objectType + 's/ventilate', requestOptions)
        .then(response => {
          if (response.ok) {
            return response.json();
          }
          throw new Error('Something went wrong');
        })
        .then(data => {
          setSuccessMessage("Ventilation succeeded.");
          setShowSuccess(true);
          handleUpdate();
        })
        .catch(() => {
          setErrorMessage("Ventilation failed.");
          setShowError(true);
        });
    }

    // EXPORT
    function handleExport() {
      updateToken();

      fetch('https://' + process.env.REACT_APP_PUBLIC_DOMAIN + '/magellan-api/api/' + objectType + 's/export', { headers: { 'Authorization': String('Bearer ' + localStorage.getItem('token')) } })
        .then(response => {
          if (response.ok) {
            return response.json();
          }
          throw new Error('Something went wrong');
        })
        .then(data => {
          setSuccessMessage("Export succeeded.");
          setShowSuccess(true);
          const jsonString = `data:text/json;charset=utf-8,${encodeURIComponent(
            JSON.stringify(data)
          )}`;
          const link = document.createElement("a");
          link.href = jsonString;
          link.download = String(objectType + "s.json");
          link.click();
        })
        .catch(() => {
          setErrorMessage("Export failed.");
          setShowError(true);
        });
    }

    function renderAddModal(){
      if (scannableItems.indexOf(objectType) > -1) {
        return  <AddScannableModal show={showAdd} 
                  handleClose={handleCloseAdd}
                  objectType = {objectType} 
                  setSuccessMessage={setSuccessMessage} 
                  setShowSuccess={setShowSuccess} 
                  setErrorMessage={setErrorMessage} 
                  setShowError={setShowError} 
                  handleUpdate={handleUpdate}
                />;
      }
      else {
        return  <AddNonScannableModal show={showAdd} 
                  handleClose={handleCloseAdd}
                  objectType = {objectType} 
                  setSuccessMessage={setSuccessMessage} 
                  setShowSuccess={setShowSuccess} 
                  setErrorMessage={setErrorMessage} 
                  setShowError={setShowError} 
                  handleUpdate={handleUpdate}
                  handleBlock={handleBlock}
                />;
      }
    }

    function renderServices(){
      if (scannableItems.indexOf(objectType) > -1) {
        return  <Col className='d-flex p-0'>
                  <Button variant="primary" onClick={handleShowAdd} className='rounded-pill fw-medium px-4 me-4'>Add {objectType === "iprange" ? ("IP range") : (objectType)}</Button>
                  <Button variant="primary" onClick={handleVentilation} className='rounded-pill fw-medium px-4 me-4 ms-3'>Ventilate</Button>
                  <Button variant="primary" onClick={handleExport} className='rounded-pill fw-medium px-4 me-4 ms-3'>Export</Button>
                  <Button variant="primary" onClick={handleShowImport} className='rounded-pill fw-medium px-4 ms-3'>Import</Button>
                </Col>;
      }
      else {
        return  <Col className='d-flex p-0'>
                  <Button variant="primary" onClick={handleShowAdd} className='rounded-pill fw-medium px-4 me-4'>Add {objectType === "ip" ? ("IP") : (objectType)}</Button>
                  <Button variant="primary" onClick={handleExport} className='rounded-pill fw-medium px-4 me-4 ms-3'>Export</Button>
                  <Button variant="primary" onClick={handleShowImport} className='rounded-pill fw-medium px-4 ms-3'>Import</Button>
                </Col>;
      }
    }

    function renderTable(){
      if (scannableItems.indexOf(objectType) > -1) {
        return  <ScannableTable 
                  objectType = {objectType} 
                  items = {items}
                  setSuccessMessage={setSuccessMessage} 
                  setShowSuccess={setShowSuccess} 
                  setErrorMessage={setErrorMessage} 
                  setShowError={setShowError} 
                  handleUpdate={handleUpdate}
                  sortField={sortField}
                  setSortField={setSortField}
                  order={order}
                  setOrder={setOrder}
                />
      }
      else {
        return  <NonScannableTable 
                  objectType = {objectType} 
                  items = {items}
                  setSuccessMessage={setSuccessMessage} 
                  setShowSuccess={setShowSuccess} 
                  setErrorMessage={setErrorMessage} 
                  setShowError={setShowError} 
                  handleUpdate={handleUpdate}
                  sortField={sortField}
                  setSortField={setSortField}
                  order={order}
                  setOrder={setOrder}
                  handleBlock={handleBlock}
                />;
      }
    }

    function renderImportModal(){
      if (scannableItems.indexOf(objectType) > -1) {
        return  <ImportScannableModal show={showImport} 
                  handleClose={handleCloseImport} 
                  objectType = {objectType} 
                  setSuccessMessage={setSuccessMessage} 
                  setShowSuccess={setShowSuccess} 
                  setErrorMessage={setErrorMessage} 
                  setShowError={setShowError} 
                  handleUpdate={handleUpdate}
                />;
      }
      else {
        return  <ImportNonScannableModal show={showImport} 
                  handleClose={handleCloseImport} 
                  objectType = {objectType} 
                  setSuccessMessage={setSuccessMessage} 
                  setShowSuccess={setShowSuccess} 
                  setErrorMessage={setErrorMessage} 
                  setShowError={setShowError} 
                  handleUpdate={handleUpdate}
                  handleBlock={handleBlock}
                />
      }
    }

    return (
      <Container className='w-75'>
        {renderAddModal()}
        {renderImportModal()}

        <Alert variant="danger" className="mb-5 mx-auto rounded-pill" show={showError}>
          <div className="px-4"><p className="mb-0"><strong>Error...</strong> {errorMessage}</p></div>
        </Alert>
      
        <Alert variant="success" className="mb-5 mx-auto rounded-pill" show={showSuccess}>
          <div className="px-4"><p className="mb-0"><strong>Success !</strong> {successMessage}</p></div>
        </Alert>
        

        {/* ------ Buttons for additional features ------*/}
        <Row className='mb-5'>
          {renderServices()}
        </Row>
        
        {/* ------ Features for table -------------------*/}
        <Row className='mb-4'>
          {/* ------ Filter -----------------------------*/}
          {objectType !== "brand" && objectType !== "iprange" &&
            <Col md="auto" className="ps-0">
              <ButtonGroup aria-label="Table filter">
                {filters.map((filter, i) => (
                  <Button key={i} active={filterValue === filter.value} variant="white" className='btn-outline-light border-2 text-secondary' onClick={() => setFilterValue(filter.value)}>{filter.name}</Button>
                ))}
              </ButtonGroup>
            </Col>
          }

          {/* ------ Number of entries selection --------*/}
          <Col className='d-flex align-items-center'>
            <NumberEntriesSelection changeNumberEntries = {setNumberEntries} />
          </Col>
          {/* ------ Search bar -------------------------*/}
          <Col lg={4} className='d-flex align-items-center pe-0'>
            <SearchBar handleSearch={handleSearch} filterValue={filterValue} />
          </Col>
        </Row>

        {/* ------ Table --------------------------------*/}
        <Row className='mb-4'>
          {renderTable()}
        </Row>
        {/* ------ Pagination ---------------------------*/}
        <Row className='mb-4'>
          <PaginationTable numberPages = {numberPages} changeCurrentPage = {setCurrentPage} currentPage = {currentPage} />
        </Row>
      </Container>
    );
  }
  
  export default ItemPage;