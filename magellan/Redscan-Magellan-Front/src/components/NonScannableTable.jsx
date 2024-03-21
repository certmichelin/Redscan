import React from 'react';
import { Table, Container, Badge, Button, Modal } from "react-bootstrap";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faEye, faBan, faTrash, faRotate, faSortUp, faSortDown, faSort } from '@fortawesome/free-solid-svg-icons'
import './GenericTable.css';
import keycloak from "../Keycloak";

function NonScannableTable({ objectType, items, setSuccessMessage, setShowSuccess, setErrorMessage, setShowError, handleUpdate, sortField, setSortField, order, setOrder, handleBlock }) {
  const [showConfirm, setShowConfirm] = React.useState(false);
  const [itemToDelete, setItemToDelete] = React.useState(false);
  const handleCloseConfirm = () => setShowConfirm(false);
  const handleShowConfirm = () => setShowConfirm(true);

  function handleDelete(item) {
    const delay = ms => new Promise(resolve => setTimeout(resolve, ms));

    if(keycloak.authenticated) {
      keycloak.updateToken(120).then(() => {
        localStorage.setItem('token', keycloak.token);
      })

      if (item.blocked) { // Unblock blocked item before deleting to avoid conflicts with blocklist
        handleBlock(item);
      }

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
    var filter = "";
    var dashboardUuid = "";

    switch(objectType) {
      case "domain":
        dashboardUuid = "04726f20-8631-11eb-8f25-273c32dc7e4b";
        filter = "domain.keyword:" + item.name;
        break;
      case "ip":
        dashboardUuid = "04726f20-8631-11eb-8f25-273c32dc7e4b";
        filter = "ip:'" + item.value + "'";
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

  function renderBlocked(blocked){
    if(blocked) {
      return <Badge pill className='negative ms-auto'>Blocked</Badge>
    }
    else {
      return <Badge pill className='positive ms-auto'>Not blocked</Badge>
    }
  }


  return (
    <Container className='card p-0 rounded rounded-3 overflow-hidden'>
      <Container>
        <Modal show={showConfirm}>
          <Modal.Header className="d-flex justify-content-center mb-3">
            <Modal.Title id="contained-modal-title-vcenter" className='fw-light text-secondary'>
              Delete {objectType === "ip" ? ("IP") : (objectType)}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <p className='w-75 mx-auto d-flex justify-content-center text-secondary'>Are you sure you want to delete {objectType === "ip" ? itemToDelete.value : itemToDelete.name} ?</p>                              
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
            <th className='text-secondary py-3' onClick={() => {handleSorting("_id")}}> {objectType === "ip" ?
                                                                                            "IP" :
                                                                                            "NAME"
                                                                                        }  {renderSort("_id")}</th>
            <th className='text-secondary py-3'>STATUS</th>
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
                <td className='text-secondary py-3'>{objectType === "ip" ?
                                                      item.value :
                                                      item.name
                                                    }</td>
                <td className='py-3 align-center'>
                  {renderBlocked(item.blocked)}
                </td>
                <td className='py-3 px-3'>
                  <Button className='btn-link text-secondary mx-2' onClick={() => {handleView(item)}} title="View"><FontAwesomeIcon icon={faEye} /></Button>
                  <Button className='btn-link text-secondary mx-2' onClick={() => {handleBlock(item)}} title="Block / Unblock"><FontAwesomeIcon icon={faBan} /></Button>
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

export default NonScannableTable;