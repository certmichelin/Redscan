import React from 'react';
import { Pagination } from 'react-bootstrap';
import './PaginationTable.css';

function PaginationTable({ numberPages, changeCurrentPage, currentPage }) {

  // Reinit of pagination
  React.useEffect(() => {
    changeCurrentPage(1);
  }, [numberPages])


  const pages = [];
  const limitMin = 2;
  const limitMax = numberPages - 1;
  if (numberPages > 6) {
    for (let i = 1; i <= limitMin; i++) {
      pages.push(
        <Pagination.Item key={i} active={i === currentPage} onClick={() => changeCurrentPage(i)}>
          {i}
        </Pagination.Item>
      );
    }


    if (currentPage == limitMin + 1) {
      pages.push(
        <Pagination.Item key={limitMin + 1} active={true} onClick={() => changeCurrentPage(limitMin + 1)}>
          {limitMin + 1}
        </Pagination.Item>
      );
      pages.push(<Pagination.Ellipsis />)
    }
    else if (currentPage > limitMin + 1 && currentPage < limitMax - 1) {
      pages.push(<Pagination.Ellipsis />)
      pages.push(
        <Pagination.Item key={currentPage} active={true} onClick={() => changeCurrentPage(currentPage)}>
          {currentPage}
        </Pagination.Item>
      );
      pages.push(<Pagination.Ellipsis />)
    }
    else if (currentPage == limitMax - 1) {
      pages.push(<Pagination.Ellipsis />)
      pages.push(
        <Pagination.Item key={limitMax - 1} active={true} onClick={() => changeCurrentPage(limitMax - 1)}>
          {limitMax - 1}
        </Pagination.Item>
      );
    }
    else {
      pages.push(<Pagination.Ellipsis />)
    }

    for (let i = limitMax; i <= numberPages; i++) {
      pages.push(
        <Pagination.Item key={i} active={i === currentPage} onClick={() => changeCurrentPage(i)}>
          {i}
        </Pagination.Item>
      );
    }

  } else {
    for (let i = 1; i <= numberPages; i++) {
      pages.push(
        <Pagination.Item key={i} active={i === currentPage} onClick={() => changeCurrentPage(i)}>
          {i}
        </Pagination.Item>
      );
    }
  }
  
  return (
    <Pagination>
      <Pagination.First onClick={() => changeCurrentPage(1)} />
      <Pagination.Prev onClick={() => currentPage === 1 ? changeCurrentPage(1) : changeCurrentPage(currentPage - 1)} />
      {pages}
      <Pagination.Next onClick={() => currentPage === numberPages ? changeCurrentPage(numberPages) : changeCurrentPage(currentPage + 1)} />
      <Pagination.Last onClick={() => changeCurrentPage(numberPages)} />
    </Pagination>
    );
  }
  
  export default PaginationTable;