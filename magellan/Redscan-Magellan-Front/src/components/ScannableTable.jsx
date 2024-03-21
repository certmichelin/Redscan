import React from 'react';
import { Table, Container, Badge, Stack, Button, Modal } from "react-bootstrap";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faEye, faPen, faTrash, faRotate, faSortUp, faSortDown, faSort } from '@fortawesome/free-solid-svg-icons'
import EditScannableModal from './EditScannableModal';
import './GenericTable.css';
import './ScannableTable.css';
import keycloak from "../Keycloak";


function ScannableTable({ objectType, items, setSuccessMessage, setShowSuccess, setErrorMessage, setShowError, handleUpdate, sortField, setSortField, order, setOrder }) {
    const [showEdit, setShowEdit] = React.useState(false);
    const handleCloseEdit = () => setShowEdit(false);
    const handleShowEdit = () => setShowEdit(true);

    const [showConfirm, setShowConfirm] = React.useState(false);
    const [itemToDelete, setItemToDelete] = React.useState(false);
    const handleCloseConfirm = () => setShowConfirm(false);
    const handleShowConfirm = () => setShowConfirm(true);

    const [itemToEdit, setItemToEdit] = React.useState();
    function handleChangeItemToEdit(item) {
      setItemToEdit(item);
    }

    function handleDelete(item) {
      const delay = ms => new Promise(resolve => setTimeout(resolve, ms));

      if(keycloak.authenticated) {
        keycloak.updateToken(120).then(() => {
          localStorage.setItem('token', keycloak.token);
        })
  
        const requestOptions = {
          method: 'DELETE',
          headers: { 'Authorization': String('Bearer ' + localStorage.getItem('token')) }
        };
        fetch(String('https://' + process.env.REACT_APP_PUBLIC_DOMAIN + '/magellan-api/api/' + objectType + 's/' + item.id), requestOptions)
          .then(async response => {
            if (!response.ok) {
              setErrorMessage("Deletion failed.");
              setShowError(true);
            }
            else { 
              setSuccessMessage("Deletion succeeded.");
              setShowSuccess(true);
              await delay(1000);
              handleUpdate();
            }
          });
      }
    }

    function handleReinject(item) {
      const delay = ms => new Promise(resolve => setTimeout(resolve, ms));

      if(keycloak.authenticated) {
        keycloak.updateToken(120).then(() => {
          localStorage.setItem('token', keycloak.token);
        })
  
        const requestOptions = {
          method: 'PUT',
          headers: { 'Authorization': String('Bearer ' + localStorage.getItem('token')) }
        };
        fetch(String('https://' + process.env.REACT_APP_PUBLIC_DOMAIN + '/magellan-api/api/' + objectType + 's/reinject/' + item.id), requestOptions)
          .then(async response => {
            if (!response.ok) {
              setErrorMessage("Reinjection failed.");
              setShowError(true);
            }
            else { 
              setSuccessMessage("Reinjection succeeded.");
              setShowSuccess(true);
              await delay(1000);
              handleUpdate();
            }
          });
      }
    }

    function handleView(item) {
      var filter = "name:" + item.name;
      var dashboardUuid = "";

      switch(objectType) {
        case "brand":
          dashboardUuid = "90e69920-922c-11eb-8d84-93aa9995dc1c";
          break;
        case "masterdomain":
          dashboardUuid = "3eef90e0-784b-11eb-ad0d-5ff1cda9ee64";
          break;
        case "iprange":
          dashboardUuid = "532f1ad0-7ca8-11ee-a06f-9f93e575a1ee";
          filter = "parent.keyword:" + item.cidr;
          break;
        default:
          dashboardUuid = "";
          break;
      }

      var url = "https://" + process.env.REACT_APP_PUBLIC_DOMAIN + "/kibana/app/dashboards#/view/" + dashboardUuid + "?_g=(filters:!((query:(match_phrase:(" + filter + ")))),refreshInterval:(pause:!t,value:60000),time:(from:now-90d%2Fd,to:now))";
      window.open(url, '_blank');
    }

    function handleSorting(field){
      if (sortField === field) {
        order === "asc" ? setOrder("desc") : setOrder("asc");
      }
      else {
        setSortField(field);
        setOrder("asc");
      }
    }


    // Conditionnal rendering
    function renderServiceLevel(serviceLevel){
      switch(serviceLevel) {
        case 1:
          return <Badge pill className='gold'>Gold</Badge>;
        case 2:
          return <Badge pill className='silver'>Silver</Badge>;
        default:
          return <Badge pill className='bronze'>Bronze</Badge>;
      }
    }

    function renderScope(scope){
      if(scope) {
        return <Badge pill className='positive ms-auto'>Scope</Badge>
      }
      else {
        return <Badge pill className='negative ms-auto'>Scope</Badge>
      }
    }
    
    function renderReviewed(reviewed){
      if(reviewed) {
        return <Badge pill className='positive me-auto'>Reviewed</Badge>
      }
      else {
        return <Badge pill className='negative me-auto'>Reviewed</Badge>
      }
    }

    function renderSort(field) {
      if (sortField === field) {
        if (order === "asc") {
          return <FontAwesomeIcon icon={faSortUp} size="xs" className='ms-2' />
        }
        else if (order === "desc") {
          return <FontAwesomeIcon icon={faSortDown} size="xs" className='ms-2' />
        }
      }
      else {
        return <FontAwesomeIcon icon={faSort} size="xs" className='ms-2' />
      }
    }


    return (
      <Container className='card p-0 rounded rounded-3 overflow-hidden'>
        <EditScannableModal show={showEdit} 
          handleClose={handleCloseEdit} 
          item={itemToEdit} 
          objectType={objectType} 
          setSuccessMessage={setSuccessMessage} 
          setShowSuccess={setShowSuccess} 
          setErrorMessage={setErrorMessage} 
          setShowError={setShowError} 
          handleUpdate={handleUpdate}
        />

        <Container>
          <Modal show={showConfirm}>
            <Modal.Header className="d-flex justify-content-center mb-3">
              <Modal.Title id="contained-modal-title-vcenter" className='fw-light text-secondary'>
                Delete {objectType === "iprange" ? ("IP range") : (objectType)}
              </Modal.Title>
            </Modal.Header>
            <Modal.Body>
              <p className='w-75 mx-auto d-flex justify-content-center text-secondary'>Are you sure you want to delete {objectType === "iprange" ? itemToDelete.cidr : itemToDelete.name} ?</p>                              
            </Modal.Body>
            <Modal.Footer className="d-flex justify-content-center">
              <Button form='editForm' type="submit" className='btn-success rounded-pill me-3' onClick={() => {handleDelete(itemToDelete); handleCloseConfirm();}}>Yes</Button>
              <Button className='btn-danger rounded-pill ms-3' onClick={handleCloseConfirm}>No</Button>
            </Modal.Footer>
          </Modal>
        </Container>

        <Table hover bordered className='mb-0'>
          <thead>
            <tr>
              <th className='text-secondary py-3' onClick={() => {handleSorting("_id")}}>{objectType === "iprange" ?
                                                                                            "CIDR" :
                                                                                            "NAME"
                                                                                          } {renderSort("_id")}</th>
              {objectType === "masterdomain" && 
                <th className='text-secondary py-3'>STATUS</th>
              }
              <th className='text-secondary py-3' onClick={() => {handleSorting("serviceLevel")}}>SERVICE LEVEL {renderSort("serviceLevel")}</th>
              <th className='text-secondary py-3' onClick={() => {handleSorting("last_scan_date")}}>LAST SCAN DATE {renderSort("last_scan_date")}</th>
              <th className='text-secondary py-3'>ACTIONS</th>
            </tr>
          </thead>
          <tbody>
            { items.length === 0 ? 
              <tr className='align-middle'>
                {objectType === "masterdomain" ?
                  <td className='text-secondary py-3' colSpan='5'><em>No items were found.</em></td>
                  : <td className='text-secondary py-3' colSpan='4'><em>No items were found.</em></td>
                }
              </tr>
            : items.map(( item ) => {
              return (
                <tr className='align-middle' key = {item.id}>
                  <td className='text-secondary py-3'>{objectType === "iprange" ?
                                                        item.cidr :
                                                        item.name
                                                      }</td>
                  {objectType === "masterdomain" && 
                    <td className='py-3 align-center'>
                      <Stack direction="horizontal" gap={2}>
                        {renderScope(item.inScope)}
                        {renderReviewed(item.reviewed)}
                      </Stack>
                    </td>
                  }
                  <td className='py-3'>
                    {renderServiceLevel(item.serviceLevel)}
                  </td>
                  <td className='text-secondary py-3'>{item.lastScanDate}</td>
                  <td className='py-3 px-3'>
                    <Button className='btn-link text-secondary mx-2' onClick={() => {handleView(item)}} title="View"><FontAwesomeIcon icon={faEye} /></Button>
                    <Button className='btn-link text-secondary mx-2' onClick={() => {handleChangeItemToEdit(item); handleShowEdit();}} title="Edit"><FontAwesomeIcon icon={faPen} /></Button>
                    <Button className='btn-link text-secondary mx-2' onClick={() => {setItemToDelete(item); handleShowConfirm();}} title="Delete"><FontAwesomeIcon icon={faTrash} /></Button>
                    <Button className='btn-link text-secondary mx-2' onClick={() => {handleReinject(item)}} title="Reinject"><FontAwesomeIcon icon={faRotate} /></Button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </Table>
      </Container>
    );
  }
  
  export default ScannableTable;